package no2.worldthreader.mixin.dimension_change.arrival;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtended {

    @Shadow public abstract Level level();

    @Shadow protected abstract TeleportTransition calculatePassengerTransition(TeleportTransition teleportTransition, Entity entity);


    @WrapOperation(
            method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getPassengers()Ljava/util/List;"
            )
    )
    private List<Entity> getPassengerOrEmpty(Entity entity, Operation<List<Entity>> original, @Local(argsOnly = true, ordinal = 1) ServerLevel destination) {
        TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$arrivingEntityInfo();
        if (currentlyArrivingEntity != null) {
            return List.of();
        } else {
            return original.call(entity);
        }
    }

    @WrapOperation(
            method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I", remap = false
            )
    )
    private int getSize(List<Entity> instance, Operation<Integer> original, @Local(argsOnly = true, ordinal = 1) ServerLevel destination) {
        TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$arrivingEntityInfo();
        if (currentlyArrivingEntity != null) {
            return currentlyArrivingEntity.passengers().size();
        } else {
            return original.call(instance);
        }
    }

    @WrapOperation(
            method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;ejectPassengers()V"
            )
    )
    private void ejectPassengers(Entity instance, Operation<Void> original, @Local(argsOnly = true, ordinal = 1) ServerLevel destination) {
        TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$arrivingEntityInfo();
        if (currentlyArrivingEntity == null) {
            original.call(instance);
        }
    }

    @Inject(
            method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V"
            )
    )
    private void placePassengers(ServerLevel serverLevel, ServerLevel serverLevel2, TeleportTransition teleportTransition, CallbackInfoReturnable<Entity> cir, @Local(argsOnly = true, ordinal = 1) ServerLevel destination, @Local(ordinal = 1) List<Entity> passengersAdded) {
        TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$arrivingEntityInfo();
        if (currentlyArrivingEntity != null) {
            if (this != (Object) currentlyArrivingEntity.oldEntityObject()) {
                throw new IllegalStateException("Worldthreader: Expected arriving entity to be the current entity!");
            }

            List<TeleportedEntityInfo> passengers = currentlyArrivingEntity.passengers();
            for (TeleportedEntityInfo passengerEntityInfo : passengers) {
                TeleportTransition passengerTeleportTransition;
                if (passengerEntityInfo.entityTransition() != null) {
                    passengerTeleportTransition = passengerEntityInfo.entityTransition();
                } else {
                    passengerTeleportTransition = this.calculatePassengerTransition(teleportTransition, passengerEntityInfo.oldEntityObject());
                }

                Entity newEntity = DimensionChangeHelper.arriveIntoWorld(passengerEntityInfo, passengerEntityInfo.oldEntityObject(), destination, (ServerLevel) this.level(), passengerTeleportTransition);
                passengersAdded.add(newEntity);
            }
        }
    }

    @WrapOperation(
            method = "restoreFrom(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;saveWithoutId(Lnet/minecraft/world/level/storage/ValueOutput;)V"
            )
    )
    private void restoreFromNbt0(Entity instance, ValueOutput valueOutput, Operation<Void> original) {
        if (this.level() instanceof ServerLevel destination) {
            TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$arrivingEntityInfo();
            if (currentlyArrivingEntity != null) {
                return;
            }
        }
        original.call(instance, valueOutput);
    }

    @WrapOperation(
            method = "restoreFrom(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/TagValueOutput;buildResult()Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    private CompoundTag restoreFromNbt1(TagValueOutput tagValueOutput, Operation<CompoundTag> original) {
        if (this.level() instanceof ServerLevel destination) {
            TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$arrivingEntityInfo();
            if (currentlyArrivingEntity != null) {
                return Objects.requireNonNull(currentlyArrivingEntity.nbtCompound()).merge(tagValueOutput.buildResult());
            }
        }
        return original.call(tagValueOutput);
    }

    @WrapOperation(
            method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;removeAfterChangingDimensions()V"
            )
    )
    private void removeOldEntity(Entity instance, Operation<Void> original, @Local(argsOnly = true, ordinal = 1) ServerLevel destination) {
        TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$arrivingEntityInfo();
        if (currentlyArrivingEntity == null) {
            original.call(instance);
        }
    }

    @WrapOperation(
            method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/PositionMoveRotation;of(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/PositionMoveRotation;"
            )
    )
    private PositionMoveRotation getPositionMoveRelation(Entity entity, Operation<PositionMoveRotation> original, @Local(argsOnly = true, ordinal = 1) ServerLevel destination) {
        TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).worldthreader$arrivingEntityInfo();
        if (currentlyArrivingEntity == null || currentlyArrivingEntity.oldEntityObject() != entity) {
            return original.call(entity);
        }
        //The velocity / rotation etc. can be modified in the old entity after it was removed from the world when teleporting since vanilla does not prevent the execution of the rest of the tick method
        return currentlyArrivingEntity.positionMoveRotation();
    }
}
