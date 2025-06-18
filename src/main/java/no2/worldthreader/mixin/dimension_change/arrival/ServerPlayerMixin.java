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
    public abstract ServerLevel level();

    @ModifyExpressionValue(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isRemoved()Z")
    )
    private boolean isRemovedAndNotArrivalPhase(boolean original, @Local(argsOnly = true) TeleportTransition teleportTransition, @Share("isArrival") LocalBooleanRef isMultithreadedPassengerArrival, @Share("isRecovery") LocalBooleanRef isRecovery) {
        if (DimensionChangeHelper.isDummy(teleportTransition)) {
            //Dummy transition only exists during departure (e.g. when using portals)
            return original;
        }

        if (!WorldThreadingManager.isMultithreadingAndCorrectThreadForWorld(teleportTransition.newLevel())) {
            //Not on destination world thread -> This is a departure, but target transition already known (e.g. pearl or command block triggered)
            // Note: This could also be an arrival at the same time, if a teleport within the same dimension is triggered from another dimension using a command block
            //Also code path for worldthreader disabled
            return original;
        }
        //This is on the destination world thread. Often that means the arrival is happening now. But sometimes it means
        // a command block or ender pearls is grabbing an entity from another dimension.

        if (WorldThreadingManager.isRecoveringTeleports(teleportTransition.newLevel())) {
            if (this.level() != teleportTransition.newLevel()) {
                throw new IllegalStateException("Worldthreader: Failed teleport recovery in wrong dimension!");
            }
            isRecovery.set(true);
            return false;
        }

        if (WorldThreadingManager.isPlacingReceivedTeleports(teleportTransition.newLevel())) {
            if (this.level() == teleportTransition.newLevel()) {
                throw new IllegalStateException("Worldthreader: Cross dimensional arrival split must be cross-dimensional!");
            }
            isMultithreadedPassengerArrival.set(true);
            return false;
        }

        //Same dimension teleportation as passenger, e.g. when a boat is teleported by a command block and the player is riding it
        //Same dimension enderpearl teleportation, command block teleportation
        //Another dimension could also be grabbing an entity from this dimension using exclusive world access
        return original;
    }

    @ModifyExpressionValue(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/TeleportTransition;missingRespawnBlock()Z")
    )
    private boolean isMissingRespawnBlockAndNotArrivalPhase(boolean original, @Share("isArrival") LocalBooleanRef isMultithreadedPassengerArrival, @Share("isRecovery") LocalBooleanRef isRecovery) {
        return original && !isMultithreadedPassengerArrival.get() && !isRecovery.get();
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
    private boolean removePlayerImmediatelyIfDeparture(ServerLevel instance, ServerPlayer serverPlayer, Entity.RemovalReason removalReason, @Share("isArrival") LocalBooleanRef isMultithreadedPassengerArrival, @Share("isRecovery") LocalBooleanRef isRecovery) {
        return !isMultithreadedPassengerArrival.get() && !isRecovery.get();
    }
}
