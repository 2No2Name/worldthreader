package no2.worldthreader.mixin.dimension_change.departure;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements EntityExtended {

    @Shadow
    public abstract ServerLevel serverLevel();

    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "HEAD")
    )
    private void check(TeleportTransition teleportTransition, CallbackInfoReturnable<ServerPlayer> cir) {
        if (DimensionChangeHelper.isDummy(teleportTransition) && !teleportTransition.asPassenger()) {
            throw new IllegalStateException("Worldthreader: Player teleported with dummy transition!");
        }
    }

    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removePlayerImmediately(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity$RemovalReason;)V", shift = At.Shift.AFTER),
            cancellable = true

    )
    private void convertSelfToTeleportedEntityInfo(TeleportTransition teleportTransition, CallbackInfoReturnable<ServerPlayer> cir) {
        if (DimensionChangeHelper.isDummy(teleportTransition)) {

            if (!WorldThreadingManager.isWrongThreadForWorld(teleportTransition.newLevel()) || !teleportTransition.asPassenger()) {
                throw new IllegalStateException("Worldthreader: Dummy transition only expected for passenger player teleports on departure level thread!");
            }

            TeleportedEntityInfo entityInfo = new TeleportedEntityInfo((Entity) (Object) this, null, null, null, null);

            //This doesn't do anything for players for now, but for called consistency with Entity teleportation code
            this.worldthreader$onEntityDepartsFromServerWorld(teleportTransition.newLevel().dimension(), this.serverLevel().dimension());

            ((ServerWorldExtended) this.serverLevel()).worldthreader$putDepartingPassengerEntityInfo(entityInfo);

            cir.setReturnValue(null);
        }
    }
}
