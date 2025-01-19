package no2.worldthreader.mixin.dimension_change.arrival;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtended {

    @Shadow private int portalCooldown;
    @Shadow public abstract void load(CompoundTag compoundTag);

    @Shadow @Nullable public PortalProcessor portalProcess;

    @Shadow public abstract Level level();

    @Shadow protected abstract TeleportTransition calculatePassengerTransition(TeleportTransition teleportTransition, Entity entity);

    //[VanillaCopy] Entity.copyFrom(Entity)
	@Override
	public void worldthreader$copyFromNBT(CompoundTag nbtCompound, Entity oldEntityObject) {
		nbtCompound.remove("Dimension");
		this.load(nbtCompound);
		this.portalCooldown = oldEntityObject.getPortalCooldown();
		this.portalProcess = oldEntityObject.portalProcess;
	}

    @WrapOperation(
            method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getPassengers()Ljava/util/List;"
            )
    )
    private List<Entity> getPassengerOrEmpty(Entity entity, Operation<List<Entity>> original, @Local(argsOnly = true) ServerLevel destination) {
        TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$getCurrentlyArrivingEntityInfo();
        if (currentlyArrivingEntity != null) {
            return List.of();
        } else {
            return original.call(entity);
        }
    }

    @WrapOperation(
            method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I", remap = false
            )
    )
    private int getSize(List<Entity> instance, Operation<Integer> original, @Local(argsOnly = true) ServerLevel destination) {
        TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$getCurrentlyArrivingEntityInfo();
        if (currentlyArrivingEntity != null) {
            return currentlyArrivingEntity.passengers().size();
        } else {
            return original.call(instance);
        }
    }

    @WrapOperation(
            method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;ejectPassengers()V"
            )
    )
    private void ejectPassengers(Entity instance, Operation<Void> original, @Local(argsOnly = true) ServerLevel destination) {
        TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$getCurrentlyArrivingEntityInfo();
        if (currentlyArrivingEntity == null) {
            original.call(instance);
        }
    }

    @Inject(
            method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V"
            )
    )
    private void placePassengers(ServerLevel serverLevel, TeleportTransition teleportTransition, CallbackInfoReturnable<Entity> cir,
                                 @Local(argsOnly = true) ServerLevel destination, @Local(ordinal = 1) List<Entity> passengersAdded) {
        TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$getCurrentlyArrivingEntityInfo();
        if (currentlyArrivingEntity != null) {
            if (this != (Object) currentlyArrivingEntity.oldEntityObject()) {
                throw new IllegalStateException("Worldthreader: Expected arriving entity to be the current entity!");
            }

            List<TeleportedEntityInfo> passengers = currentlyArrivingEntity.passengers();
            for (TeleportedEntityInfo passengerEntityInfo : passengers) {
                TeleportTransition passengerTeleportTransition = this.calculatePassengerTransition(teleportTransition, passengerEntityInfo.oldEntityObject());

                Entity newEntity = DimensionChangeHelper.arriveIntoWorld(passengerEntityInfo, passengerEntityInfo.oldEntityObject(), destination, (ServerLevel) this.level(), passengerTeleportTransition);
                passengersAdded.add(newEntity);
            }
        }
    }

    @WrapOperation(
            method = "restoreFrom(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;saveWithoutId(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    private CompoundTag restoreFromNBT(Entity oldEntity, CompoundTag compoundTag, Operation<CompoundTag> original) {
        if (this.level() instanceof ServerLevel destination) {
            TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$getCurrentlyArrivingEntityInfo();
            if (currentlyArrivingEntity != null) {
                return currentlyArrivingEntity.nbtCompound().merge(compoundTag);
            }
        }
        return original.call(oldEntity, compoundTag);
    }

    @WrapOperation(
            method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;removeAfterChangingDimensions()V"
            )
    )
    private void removeOldEntity(Entity instance, Operation<Void> original, @Local(argsOnly = true) ServerLevel destination) {
        TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$getCurrentlyArrivingEntityInfo();
        if (currentlyArrivingEntity == null) {
            original.call(instance);
        }
    }
}
