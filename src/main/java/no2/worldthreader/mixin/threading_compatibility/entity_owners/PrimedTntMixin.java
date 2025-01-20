package no2.worldthreader.mixin.threading_compatibility.entity_owners;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.UnsafeOwnerAccess;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(PrimedTnt.class)
public abstract class PrimedTntMixin extends Entity implements TraceableEntity, UnsafeOwnerAccess {

    @Shadow private @Nullable LivingEntity owner;

    public PrimedTntMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    //If something gets the cross-dimensional owner of the entity, worldthreader will have to fall back to serial
    // execution, as worldthreader has no way to detect whether the access is safe or not. For safe accesses, the
    // interface UnsafeOwnerAccess provides a method.
    @Inject(
            method = "getOwner()Lnet/minecraft/world/entity/LivingEntity;", at = @At("HEAD")
    )
    public void getOwner(CallbackInfoReturnable<Entity> cir) {
        Entity owner = this.owner;
        if (owner != null && owner.level() instanceof ServerLevel otherLevel && otherLevel != this.level() && WorldThreadingManager.isWorldAccessDenied(otherLevel)) {
            //Directly accessing the other level on the MinecraftServer will trigger worldthreader's serial fallback
            Objects.requireNonNull(this.level().getServer()).getAllLevels();
        }
    }

    @Override
    public @Nullable LivingEntity worldthreader$getCachedOwnerUnsafe() {
        return this.owner;
    }
}
