package no2.worldthreader.mixin.dimension_change.departure;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {

    @Shadow public boolean forceTickAfterTeleportToDuplicate;

    public FallingBlockEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At(value = "RETURN")
    )
    public void worldthreader$allowFallingBlockDuplication(TeleportTransition teleportTransition, CallbackInfoReturnable<Entity> cir, @Local boolean changedFromOrToEnd, @Local Entity newEntity) {
        if (changedFromOrToEnd && newEntity == null && this.level() instanceof ServerLevel serverLevel
                && ((MinecraftServerExtended) serverLevel.getServer()).worldthreader$isTickMultithreaded()) {
            this.forceTickAfterTeleportToDuplicate = true;
        }
    }
}
