package no2.worldthreader.mixin.dimension_change.departure;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements EntityExtended {

    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Shadow
    public abstract ServerLevel level();


    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "HEAD")
    )
    private void check(TeleportTransition teleportTransition, CallbackInfoReturnable<ServerPlayer> cir) {
        //noinspection ConstantValue
        if (DimensionChangeHelper.isDummy(teleportTransition) && !teleportTransition.asPassenger() && ((Class<?>) this.getClass() == ServerPlayer.class)) {
            throw new IllegalStateException("Worldthreader: Player teleported with dummy transition!");
        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removePlayerImmediately(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity$RemovalReason;)V", shift = At.Shift.AFTER),
            cancellable = true

    )
    private void convertSelfToTeleportedEntityInfo(TeleportTransition teleportTransition, CallbackInfoReturnable<ServerPlayer> cir) {
        if (DimensionChangeHelper.shouldConvertSelfToTeleportedEntityInfo(teleportTransition.newLevel())) {
            TeleportTransition nonDummyTransition = DimensionChangeHelper.isDummy(teleportTransition) ? null : teleportTransition;
            PortalProcessor dummyTransitionHandler = nonDummyTransition == null && !teleportTransition.asPassenger() ? Objects.requireNonNull(this.portalProcess) : null;
            TeleportedEntityInfo entityInfo = new TeleportedEntityInfo(this, null, nonDummyTransition, dummyTransitionHandler, null, null, List.of());

            //This doesn't do anything for players for now, but for called consistency with Entity teleportation code
            this.worldthreader$onEntityDepartsFromServerWorld(teleportTransition.newLevel().dimension(), this.level().dimension());

            if (teleportTransition.asPassenger()) {
                ((ServerWorldExtended) this.level()).worldthreader$putDepartingPassengerEntityInfo(entityInfo);
            } else {
                //Support for command blocks such as /execute in other_dimension run tp @p ~ ~ ~ and datapacks
                //Support for carpet mod players using portals
                ((ServerWorldExtended) teleportTransition.newLevel()).worldthreader$receiveTeleportedEntity(this.level().dimension(), entityInfo);
            }
            cir.setReturnValue(null);
        }
    }
}
