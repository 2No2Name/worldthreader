package no2.worldthreader.mixin.dimension_change.arrival;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {


    @Shadow
    public abstract ServerLevel serverLevel();

    @ModifyExpressionValue(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isRemoved()Z")
    )
    private boolean isRemovedAndNotArrivalPhase(boolean original, @Local(argsOnly = true) TeleportTransition teleportTransition, @Share("isArrival") LocalBooleanRef isMultithreadedPassengerArrival) {
        if (!DimensionChangeHelper.isDummy(teleportTransition) && WorldThreadingManager.isMultithreadingAndCorrectThreadForWorld(teleportTransition.newLevel())) {
            if (!teleportTransition.asPassenger() || this.serverLevel() == teleportTransition.newLevel()) {
                throw new IllegalStateException("Worldthreader: On destination world teleport call only expected for cross-world passenger player teleports!");
            }
            isMultithreadedPassengerArrival.set(true);
            return false;
        }
        isMultithreadedPassengerArrival.set(false);
        return original;
    }

    @ModifyExpressionValue(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/TeleportTransition;missingRespawnBlock()Z")
    )
    private boolean isMissingRespawnBlockAndNotArrivalPhase(boolean original, @Share("isArrival") LocalBooleanRef isMultithreadedPassengerArrival) {
        return original && !isMultithreadedPassengerArrival.get();
    }

    @WrapOperation(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;createCommonSpawnInfo(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/network/protocol/game/CommonPlayerSpawnInfo;"
            ),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;isChangingDimension:Z", opcode = Opcodes.PUTFIELD),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V")
            )
    )
    private CommonPlayerSpawnInfo createCommonSpawnInfoIfDeparture(ServerPlayer instance, ServerLevel serverLevel, Operation<CommonPlayerSpawnInfo> original, @Share("isArrival") LocalBooleanRef isMultithreadedPassengerArrival) {
        if (isMultithreadedPassengerArrival.get()) {
            return null;
        }
        return original.call(instance, serverLevel);
    }

    @WrapWithCondition(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"
            ),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;isChangingDimension:Z", opcode = Opcodes.PUTFIELD),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V")
            )
    )
    private boolean sendIfDeparture(ServerGamePacketListenerImpl instance, Packet<?> packet, @Share("isArrival") LocalBooleanRef isMultithreadedPassengerArrival) {
        return !isMultithreadedPassengerArrival.get();
    }

    @WrapWithCondition(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V"
            ),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;isChangingDimension:Z", opcode = Opcodes.PUTFIELD),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V")
            )
    )
    private boolean sendPlayerPermissionLevelIfDeparture(PlayerList instance, ServerPlayer serverPlayer, @Share("isArrival") LocalBooleanRef isMultithreadedPassengerArrival) {
        return !isMultithreadedPassengerArrival.get();
    }

    @WrapWithCondition(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;removePlayerImmediately(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity$RemovalReason;)V"
            ),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;isChangingDimension:Z", opcode = Opcodes.PUTFIELD),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V")
            )
    )
    private boolean unsetRemovedIfDeparture(ServerLevel instance, ServerPlayer serverPlayer, Entity.RemovalReason removalReason, @Share("isArrival") LocalBooleanRef isMultithreadedPassengerArrival) {
        return !isMultithreadedPassengerArrival.get();
    }
}
