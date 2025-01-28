package no2.worldthreader.mixin.threading_compatibility.entity_owners;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.UnsafeOwnerAccess;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Vex.class)
public abstract class VexMixin extends Entity implements TraceableEntity, UnsafeOwnerAccess {

    @Shadow @Nullable Mob owner;

    public VexMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    //If something gets the cross-dimensional owner of the entity, worldthreader will have to fall back to serial
    // execution, as worldthreader has no way to detect whether the access is safe or not. For safe accesses, the
    // interface UnsafeOwnerAccess provides a method.
    @Inject(
            method = "getOwner()Lnet/minecraft/world/entity/Mob;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getOwner(CallbackInfoReturnable<Entity> cir) {
        Entity owner = this.owner;
        if (owner != null && owner.level() instanceof ServerLevel otherLevel && otherLevel != this.level() && WorldThreadingManager.hasToAcquireExclusiveAccessBeforeAccessing(otherLevel)) {
            //Disallow cross-dimensional owner access, as Vexes will attack the target of the owner, which may be almost any entity in the different dimension, which is not threadsafe
            cir.setReturnValue(null);
        }
    }

    @Override
    public @Nullable Mob worldthreader$getCachedOwnerUnsafe() {
        return this.owner;
    }
}
