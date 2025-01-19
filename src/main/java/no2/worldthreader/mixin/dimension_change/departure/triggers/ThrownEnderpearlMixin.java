package no2.worldthreader.mixin.dimension_change.departure.triggers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin {

    @Shadow
    private static native boolean isAllowedToTeleportOwner(Entity entity, Level level);

    @Redirect(
            method = "onHit(Lnet/minecraft/world/phys/HitResult;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownEnderpearl;isAllowedToTeleportOwner(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;)Z")
    )
    private boolean requestOwnerTeleport(Entity entity, Level level) {
        if (entity.level().dimension() == level.dimension()) {
            return isAllowedToTeleportOwner(entity, level);
        }
        DimensionChangeHelper.requestEnderPearlTeleportFromDestinationWorld((ThrownEnderpearl) (Object) this, entity);
        return false;
    }


    //TODO make the getOwner search threadsafe
    //Replace in the ender pearl death, tickets and teleport attempt...
}
