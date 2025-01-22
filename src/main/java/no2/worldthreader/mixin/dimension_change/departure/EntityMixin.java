package no2.worldthreader.mixin.dimension_change.departure;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import no2.worldthreader.common.thread.ThreadLocals;
import no2.worldthreader.common.thread.WorldThreadingManager;
import no2.worldthreader.common.tuples.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtended {

	@Shadow public abstract Level level();

	@Shadow protected abstract void removeAfterChangingDimensions();

	@Shadow @Nullable public PortalProcessor portalProcess;

	@Shadow protected abstract TeleportTransition calculatePassengerTransition(TeleportTransition teleportTransition, Entity entity);

	@ModifyExpressionValue(
			method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/TeleportTransition;asPassenger()Z")
	)
	private boolean disallowCrossDimensionalPassengerTeleport(boolean isPassenger, @Local(argsOnly = true) TeleportTransition teleportTransition, @Local(ordinal = 0) boolean crossDimensional, @Local(ordinal = 1) ServerLevel destination) {
		if (crossDimensional && isPassenger && WorldThreadingManager.isWrongThreadForWorld(destination)) {
			DimensionChangeHelper.expectDummy(teleportTransition);
		}
        return isPassenger;
    }

	@Redirect(
			method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;calculatePassengerTransition(Lnet/minecraft/world/level/portal/TeleportTransition;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/level/portal/TeleportTransition;")
	)
	private TeleportTransition getDummyPassengerTransition(Entity instance, TeleportTransition teleportTransition, Entity entity) {
		if (DimensionChangeHelper.isDummy(teleportTransition)) {
			return teleportTransition.transitionAsPassenger();
		}
		return ((EntityMixin) (Object) instance).calculatePassengerTransition(teleportTransition, entity);
	}

	@WrapOperation(
			method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;")
	)
	public Entity convertPassengersToTeleportedEntityInfos(Entity passenger, TeleportTransition teleportTransition, Operation<Entity> original, @Local(argsOnly = true) ServerLevel destination, @Share("PassengerInfos") LocalRef<List<TeleportedEntityInfo>> passengerInfos) {
		if (WorldThreadingManager.isWrongThreadForWorld(destination)) {
			DimensionChangeHelper.expectDummy(teleportTransition);

			if (passengerInfos.get() == null) {
                passengerInfos.set(new ArrayList<>());
            }

			//This call should call worldthreader$setCurrentlyDepartingEntityInfo and return null, unlike vanilla, which returns the new entity for the destination level
			var ret = original.call(passenger, teleportTransition);
			if (ret != null) {
                throw new IllegalStateException("Worldthreader: Teleportation was finished unexpectedly!");
            }
			TeleportedEntityInfo teleportedEntityInfo = ((ServerWorldExtended) this.level()).worldthreader$removeDepartingEntityInfo();
			if (teleportedEntityInfo == null) {
				//Teleport of passenger failed, do not add to list of received passengers as vanilla
				return null;
			}

			passengerInfos.get().add(teleportedEntityInfo);
			return null; //Return value doesn't matter, we don't use vanilla's passenger list anymore
		} else {
            return original.call(passenger, teleportTransition);
        }
    }


	@Inject(
			method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/Profiler;get()Lnet/minecraft/util/profiling/ProfilerFiller;"),
			cancellable = true

	)
	private void convertSelfToTeleportedEntityInfo(ServerLevel destination, TeleportTransition teleportTransition, CallbackInfoReturnable<Entity> cir, @Share("PassengerInfos") LocalRef<List<TeleportedEntityInfo>> passengerInfos) {
		if (WorldThreadingManager.isWrongThreadForWorld(destination)) {

			cir.setReturnValue(null);
			DimensionChangeHelper.expectDummy(teleportTransition);

			boolean isPassenger = teleportTransition.asPassenger();

			CompoundTag entityNBT = copyFromToNBT((Entity) (Object) this);
			Direction.Axis portalAxis = null;
			Vec3 inPortalPos = null;

            if (!isPassenger && this.portalProcess != null && this.portalProcess.isSamePortal((Portal) Blocks.NETHER_PORTAL)) {
				//See NetherPortalBlockMixin: This call populates ThreadLocals.NETHER_PORTAL_POSITION_INFO if the last 3 parameters are null.
				//noinspection DataFlowIssue
                NetherPortalBlock.getDimensionTransitionFromExit((Entity) (Object) this, this.portalProcess.getEntryPosition(), null, null, null);
				Pair<Direction.Axis, Vec3> portalAxisAndRelativePosition = ThreadLocals.NETHER_PORTAL_POSITION_INFO.get();
				ThreadLocals.NETHER_PORTAL_POSITION_INFO.remove();

				portalAxis = portalAxisAndRelativePosition.first();
				inPortalPos = portalAxisAndRelativePosition.second();
			}

			List<TeleportedEntityInfo> teleportedEntityInfos = passengerInfos.get();
			if (teleportedEntityInfos == null) {
                teleportedEntityInfos = List.of();
            }
			//noinspection DataFlowIssue
            TeleportedEntityInfo entityInfo = new TeleportedEntityInfo((Entity) (Object) this, entityNBT, portalAxis, inPortalPos, teleportedEntityInfos);

			// [VanillaCopy] teleportCrossDimensions
			this.removeAfterChangingDimensions();
			this.worldthreader$onEntityDepartsFromServerWorld(destination.dimension(), this.level().dimension());

			if (isPassenger) {
				((ServerWorldExtended) this.level()).worldthreader$putDepartingEntityInfo(entityInfo);
			} else {
                ((ServerWorldExtended) destination).worldthreader$receiveTeleportedEntity(this.level().dimension(), entityInfo);
            }
		}
	}


	//[VanillaCopy] Entity.copyFrom(Entity)
	@Unique
	private static CompoundTag copyFromToNBT(Entity original) {
        return original.saveWithoutId(new CompoundTag());
	}
}
