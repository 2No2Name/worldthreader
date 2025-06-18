package no2.worldthreader.mixin.threading_compatibility.entity_owners;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.UnsafeOwnerAccess;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vex.class)
public abstract class VexMixin extends Entity implements TraceableEntity, UnsafeOwnerAccess<Mob> {

    @Shadow
    private @Nullable EntityReference<Mob> owner;

    public VexMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    //If something gets the cross-dimensional owner of the entity, worldthreader will have to fall back to serial
    // execution, as worldthreader has no way to detect whether the access is safe or not. For safe accesses, the
    // interface UnsafeOwnerAccess provides a method.
    @WrapMethod(
            method = "getOwner()Lnet/minecraft/world/entity/Mob;"
    )
    public Mob getOwner(Operation<Mob> original) {
        Mob owner = original.call();
        if (owner != null && owner.level() instanceof ServerLevel otherLevel && otherLevel != this.level() && WorldThreadingManager.hasToAcquireExclusiveAccessBeforeAccessing(otherLevel)) {
            //Disallow cross-dimensional owner access, as Vexes will attack the target of the owner, which may be almost any entity in the different dimension, which is not threadsafe
            return null;
        }
        return owner;
    }

    @Override
    public @Nullable EntityReference<Mob> worldthreader$getCachedOwnerUnsafe() {
        return this.owner;
    }
}
