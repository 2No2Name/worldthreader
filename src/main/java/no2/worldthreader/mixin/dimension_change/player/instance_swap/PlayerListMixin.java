package no2.worldthreader.mixin.dimension_change.player.instance_swap;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import no2.worldthreader.common.mixin_support.interfaces.ServerPlayerInstanceSwapper;
import no2.worldthreader.common.thread.ThreadLocals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements ServerPlayerInstanceSwapper {


    @Shadow
    public abstract ServerPlayer respawn(ServerPlayer serverPlayer, boolean bl, Entity.RemovalReason removalReason);

    @Override
    public ServerPlayer worldthreader$swapRemovedPlayerWithNewCopy(ServerPlayer previousPlayer, ServerLevel newLevel) {
        if (ThreadLocals.PLAYER_SWAP_4_LEVEL.get() != null) {
            throw new IllegalStateException("Player swapping is already happening!");
        }
        ThreadLocals.PLAYER_SWAP_4_LEVEL.set(newLevel);
        ServerPlayer newPlayer = this.respawn(previousPlayer, true, null);

        //Additional Stuff
        if (newPlayer.isChangingDimension() != previousPlayer.isChangingDimension()) {
            newPlayer.isChangingDimension = previousPlayer.isChangingDimension();
        }
        if (newPlayer.getPortalCooldown() != previousPlayer.getPortalCooldown()) {
            newPlayer.setPortalCooldown(previousPlayer.getPortalCooldown());
        }
        if (newPlayer.enderPearls.isEmpty() && !previousPlayer.enderPearls.isEmpty()) {
            for (var enderpearl : previousPlayer.enderPearls) {
                newPlayer.registerEnderPearl(enderpearl);
                //Setting the owner in the enderpearl might be a good idea as well, but since worldthreader
                // modifies the getOwner function such that the outdated previous value is immediately replaced on
                // access, it doesn't make a difference (unless other mods directly use the field)
            }
        }

        //Others fields like this might be relevant but hard to track down, not doing it for now
//        newPlayer.startingToFallPosition = previousPlayer.startingToFallPosition;

        ThreadLocals.PLAYER_SWAP_4_LEVEL.remove();
        return newPlayer;
    }

    @WrapWithCondition(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removePlayerImmediately(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity$RemovalReason;)V")
    )
    private boolean isNotSwap(ServerLevel instance, ServerPlayer serverPlayer, Entity.RemovalReason removalReason, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        boolean shouldExecuteAll = removalReason != null || ThreadLocals.PLAYER_SWAP_4_LEVEL.get() == null;
        isNotPlayerSwap.set(shouldExecuteAll);
        return shouldExecuteAll;
    }

    @WrapOperation(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;")
    )
    private TeleportTransition findRespawnPositionAndUseSpawnBlockIfNotSwap(ServerPlayer instance, boolean bl, TeleportTransition.PostTeleportTransition postTeleportTransition, Operation<TeleportTransition> original, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        if (isNotPlayerSwap.get()) {
            return original.call(instance, bl, postTeleportTransition);
        }
        return null;
    }

    @WrapOperation(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/TeleportTransition;newLevel()Lnet/minecraft/server/level/ServerLevel;")
    )
    private ServerLevel newLevel(TeleportTransition instance, Operation<ServerLevel> original, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        if (isNotPlayerSwap.get()) {
            return original.call(instance);
        }
        return ThreadLocals.PLAYER_SWAP_4_LEVEL.get();
    }

    @WrapOperation(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/TeleportTransition;missingRespawnBlock()Z"), require = 2
    )
    private boolean missingRespawnBlockIfNotSwap(TeleportTransition instance, Operation<Boolean> original, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        if (isNotPlayerSwap.get()) {
            return original.call(instance);
        }
        return false;
    }

    @WrapOperation(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/TeleportTransition;position()Lnet/minecraft/world/phys/Vec3;")
    )
    private Vec3 positionIfNotSwap(TeleportTransition instance, Operation<Vec3> original, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        if (isNotPlayerSwap.get()) {
            return original.call(instance);
        }
        return Vec3.ZERO;
    }

    @WrapOperation(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/TeleportTransition;xRot()F"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/TeleportTransition;yRot()F")
            }
    )
    private float rotationIfNotSwap(TeleportTransition instance, Operation<Float> original, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        if (isNotPlayerSwap.get()) {
            return original.call(instance);
        }
        return 0.0f;
    }

    @WrapWithCondition(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;moveTo(DDDFF)V")
    )
    private boolean isNotSwap(ServerPlayer instance, double v, double v1, double v2, float v3, float v4, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        return isNotPlayerSwap.get();
    }

    @WrapOperation(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getLevelData()Lnet/minecraft/world/level/storage/LevelData;")

    )
    private LevelData getLevelDataIfNotSwap(ServerLevel instance, Operation<LevelData> original, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        if (isNotPlayerSwap.get()) {
            return original.call(instance);
        }
        return null;
    }

    @WrapOperation(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;createCommonSpawnInfo(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/network/protocol/game/CommonPlayerSpawnInfo;")

    )
    private CommonPlayerSpawnInfo createCommonSpawnInfoIfNotSwap(ServerPlayer instance, ServerLevel serverLevel, Operation<CommonPlayerSpawnInfo> original, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        if (isNotPlayerSwap.get()) {
            return original.call(instance, serverLevel);
        }
        return null;
    }

    @WrapOperation(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;")
    )
    private BlockPos getSharedSpawnPosIfNotSwap(ServerLevel instance, Operation<BlockPos> original, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        if (isNotPlayerSwap.get()) {
            return original.call(instance);
        }
        return null;
    }

    @WrapOperation(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnAngle()F")
    )
    private float getSharedSpawnAngleIfNotSwap(ServerLevel instance, Operation<Float> original, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        if (isNotPlayerSwap.get()) {
            return original.call(instance);
        }
        return 0.0f;
    }

    @WrapOperation(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelData;getDifficulty()Lnet/minecraft/world/Difficulty;")
    )
    private Difficulty getDifficultyIfNotSwap(LevelData instance, Operation<Difficulty> original, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        if (isNotPlayerSwap.get()) {
            return original.call(instance);
        }
        return null;
    }

    @WrapOperation(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelData;isDifficultyLocked()Z")
    )
    private boolean isDifficultyLockedIfNotSwap(LevelData instance, Operation<Boolean> original, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        if (isNotPlayerSwap.get()) {
            return original.call(instance);
        }
        return false;
    }

    @WrapWithCondition(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"),
            require = 6
    )
    private boolean ifNotSwap(ServerGamePacketListenerImpl instance, Packet<?> packet, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        return isNotPlayerSwap.get();
    }

    @WrapWithCondition(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;sendActivePlayerEffects(Lnet/minecraft/server/level/ServerPlayer;)V"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V"),
            }, require = 2
    )
    private boolean ifNotSwap(PlayerList instance, ServerPlayer serverPlayer, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        return isNotPlayerSwap.get();
    }

    @WrapWithCondition(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;sendLevelInfo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V")
    )
    private boolean ifNotSwap(PlayerList instance, ServerPlayer serverPlayer, ServerLevel serverLevel, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        return isNotPlayerSwap.get();
    }

    @WrapWithCondition(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addRespawnedPlayer(Lnet/minecraft/server/level/ServerPlayer;)V")
    )
    private boolean ifNotSwap(ServerLevel instance, ServerPlayer serverPlayer, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap) {
        return isNotPlayerSwap.get();
    }

    @Inject(
            method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setHealth(F)V", shift = At.Shift.AFTER),
            cancellable = true
    )
    private void cancelIfSwap(CallbackInfoReturnable<ServerPlayer> cir, @Share("IsNotPlayerSwap") LocalBooleanRef isNotPlayerSwap, @Local(ordinal = 0, argsOnly = true) ServerPlayer previousPlayer, @Local(ordinal = 1) ServerPlayer newPlayer) {
        if (!isNotPlayerSwap.get()) {
            cir.setReturnValue(newPlayer);
        }
    }
}
