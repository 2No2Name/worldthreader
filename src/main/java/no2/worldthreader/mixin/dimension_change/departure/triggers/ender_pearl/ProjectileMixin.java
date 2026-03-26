package no2.worldthreader.mixin.dimension_change.departure.triggers.ender_pearl;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
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
}
