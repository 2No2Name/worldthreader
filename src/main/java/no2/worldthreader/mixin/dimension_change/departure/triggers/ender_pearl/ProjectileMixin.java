package no2.worldthreader.mixin.dimension_change.departure.triggers.ender_pearl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.EntityReferenceExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity implements TraceableEntity {

    @Shadow
    public abstract @Nullable Entity getOwner();

    @Shadow
    @Nullable
    public EntityReference<Entity> owner;

    public ProjectileMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }
    @Redirect(
            method = {"setOwner(Lnet/minecraft/world/entity/EntityReference;)V", "restoreFrom(Lnet/minecraft/world/entity/Entity;)V"}, require = 2,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/Projectile;owner:Lnet/minecraft/world/entity/EntityReference;", opcode = Opcodes.PUTFIELD)
    )
    public void setCachedOwnerWrapped(Projectile theProjectile, @Nullable EntityReference<Entity> entityReference) { //Overwritten by ThrownEnderpearlMixin
        ((ProjectileMixin) (Object) theProjectile).owner = entityReference;
    }

    /**
     * Avoid unnecessary interdimensional thread safety
     * This removes vanilla bugged interdimensional communication in leftOwner
     *
     * @return owner, if it is in the same dimension, null otherwise
     */
    @WrapOperation(
            method = {"canHitEntity", "isOutsideOwnerCollisionRange"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;getOwner()Lnet/minecraft/world/entity/Entity;")
    )
    private Entity getOwnerInSameDimension(Projectile instance, Operation<Entity> original) {
        Level level = this.level();
        if (level instanceof ServerLevel serverLevel) {
            WorldThreadingManager worldThreadingManager = WorldThreadingManager.get(serverLevel);
            if (worldThreadingManager != null && worldThreadingManager.isMultiThreadedPhase()) {
                //noinspection unchecked
                return instance.owner == null ? null : ((EntityReferenceExtended<Entity>) (Object) instance.owner).worldthreader$getEntitySameDimension(level, Entity.class);
            }
        }

        return original.call(instance);
    }
}


//TODO getAddEntityPacket, mayInteract
