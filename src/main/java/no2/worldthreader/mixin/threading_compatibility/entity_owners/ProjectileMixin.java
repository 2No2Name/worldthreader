package no2.worldthreader.mixin.threading_compatibility.entity_owners;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.UnsafeOwnerAccess;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity implements TraceableEntity, UnsafeOwnerAccess<Entity> {

    @Shadow
    public abstract @Nullable Entity getOwner();

    @Shadow
    @Nullable
    public EntityReference<Entity> owner;

    public ProjectileMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    //If something gets the cross-dimensional owner of the entity, worldthreader will have to fall back to serial
    // execution, as worldthreader has no way to detect whether the access is safe or not. For safe accesses, the
    // interface UnsafeOwnerAccess provides a method.
    @WrapMethod(
            method = "getOwner()Lnet/minecraft/world/entity/Entity;"
    )
    public Entity getOwner(Operation<Entity> original) {
        Entity owner = original.call();
        if (owner != null && owner.level() instanceof ServerLevel otherLevel && otherLevel != this.level() && WorldThreadingManager.hasToAcquireExclusiveAccessBeforeAccessing(otherLevel)) {
            //Directly accessing the other level on the MinecraftServer will trigger worldthreader's serial fallback
            Objects.requireNonNull(this.level().getServer()).getAllLevels();
        }
        return owner;
    }

    @Override
    public @Nullable EntityReference<Entity> worldthreader$getCachedOwnerUnsafe() {
        return this.owner;
    }

    @Redirect(
            method = {"setOwner(Lnet/minecraft/world/entity/EntityReference;)V", "restoreFrom(Lnet/minecraft/world/entity/Entity;)V"}, require = 2,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/Projectile;owner:Lnet/minecraft/world/entity/EntityReference;", opcode = Opcodes.PUTFIELD)
    )
    public void setCachedOwnerWrapped(Projectile theProjectile, @Nullable EntityReference<Entity> entityReference) { //Overwritten by ThrownEnderpearlMixin
        ((ProjectileMixin) (Object) theProjectile).owner = entityReference;
    }
}
