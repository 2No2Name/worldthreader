package no2.worldthreader.mixin.threading_compatibility.entity_owners;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.UnsafeOwnerAccess;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(PrimedTnt.class)
public abstract class PrimedTntMixin extends Entity implements TraceableEntity, UnsafeOwnerAccess<LivingEntity> {


    @Shadow
    private @Nullable EntityReference<LivingEntity> owner;

    public PrimedTntMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    //If something gets the cross-dimensional owner of the entity, worldthreader will have to fall back to serial
    // execution, as worldthreader has no way to detect whether the access is safe or not. For safe accesses, the
    // interface UnsafeOwnerAccess provides a method.
    @WrapMethod(
            method = "getOwner()Lnet/minecraft/world/entity/LivingEntity;"
    )
    public LivingEntity getOwner(Operation<LivingEntity> original) {
        LivingEntity owner = original.call();
        if (owner != null && owner.level() instanceof ServerLevel otherLevel && otherLevel != this.level() && WorldThreadingManager.hasToAcquireExclusiveAccessBeforeAccessing(otherLevel)) {
            //Directly accessing the other level on the MinecraftServer will trigger worldthreader's serial fallback
            Objects.requireNonNull(this.level().getServer()).getAllLevels();
        }
        return owner;
    }

    @Override
    public @Nullable EntityReference<LivingEntity> worldthreader$getCachedOwnerUnsafe() {
        return this.owner;
    }
}
