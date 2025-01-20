package no2.worldthreader.mixin.threading_compatibility.entity_owners;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.UnsafeOwnerAccess;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity implements TraceableEntity, UnsafeOwnerAccess {
    @Shadow private @Nullable Entity cachedOwner;

    @Shadow public abstract void setOwner(@Nullable Entity entity);

    public ProjectileMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    //If something gets the cross-dimensional owner of the entity, worldthreader will have to fall back to serial
    // execution, as worldthreader has no way to detect whether the access is safe or not. For safe accesses, the
    // interface UnsafeOwnerAccess provides a method.
    @Inject(
            method = "getOwner()Lnet/minecraft/world/entity/Entity;", at = @At("HEAD")
    )
    public void getOwner(CallbackInfoReturnable<Entity> cir) {
        Entity owner = this.cachedOwner;
        if (owner != null && owner.level() instanceof ServerLevel otherLevel && otherLevel != this.level() && WorldThreadingManager.isWorldAccessDenied(otherLevel)) {
            //Directly accessing the other level on the MinecraftServer will trigger worldthreader's serial fallback
            Objects.requireNonNull(this.level().getServer()).getAllLevels();
        }
    }

    @Override
    public @Nullable Entity worldthreader$getCachedOwnerUnsafe() {
        return this.cachedOwner;
    }

    @Redirect(
            method =  {"setOwner(Lnet/minecraft/world/entity/Entity;)V", "setOwnerThroughUUID(Ljava/util/UUID;)V"}, require = 2,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/Projectile;cachedOwner:Lnet/minecraft/world/entity/Entity;")
    )
    public void handleSetCachedOwner(Projectile theProjectile, Entity cachedOwner) {
        this.setCachedOwnerWrapped(theProjectile, cachedOwner);
    }
    @Redirect(
            method =  {"getOwner()Lnet/minecraft/world/entity/Entity;"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/Projectile;cachedOwner:Lnet/minecraft/world/entity/Entity;", ordinal = 3)
    )
    public void handleSetCachedOwner1(Projectile theProjectile, Entity cachedOwner) {
        this.setCachedOwnerWrapped(theProjectile, cachedOwner);
    }
    @Redirect(
            method =  {"restoreFrom(Lnet/minecraft/world/entity/Entity;)V"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/Projectile;cachedOwner:Lnet/minecraft/world/entity/Entity;", ordinal = 1)
    )
    public void handleSetCachedOwner2(Projectile theProjectile, Entity cachedOwner) {
        this.setCachedOwnerWrapped(theProjectile, cachedOwner);
    }

    @Unique
    protected void setCachedOwnerWrapped(Projectile theProjectile, Entity cachedOwner) {
        ((ProjectileMixin) (Object) theProjectile).cachedOwner = cachedOwner;
    }
}
