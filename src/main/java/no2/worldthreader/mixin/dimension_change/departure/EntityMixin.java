package no2.worldthreader.mixin.dimension_change.departure;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import no2.worldthreader.common.thread.ThreadLocals;
import no2.worldthreader.common.thread.WorldThreadingManager;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import no2.worldthreader.common.tuples.Pair;
import no2.worldthreader.common.tuples.Triplet;
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
import java.util.Objects;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtended {

	@Shadow public abstract Level level();

	@Shadow protected abstract void removeAfterChangingDimensions();

	@Shadow @Nullable public PortalProcessor portalProcess;

	@Redirect(
			method = "handlePortal()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/portal/TeleportTransition;newLevel()Lnet/minecraft/server/level/ServerLevel;"
			)
	)
	private ServerLevel getLevelFromDummy(TeleportTransition teleportTransition) {
		if (DimensionChangeHelper.isDummy(teleportTransition)) {
			ResourceKey<Level> destinationWorldKey = DimensionChangeHelper.getDestinationFromNonPassengerDummyElseNull(teleportTransition);
			return ((MinecraftServerExtended) Objects.requireNonNull(this.level().getServer())).worldthreader$getLevelUnsynchronized(Objects.requireNonNull(destinationWorldKey));
		}
		return teleportTransition.newLevel();
	}

	@Redirect(
			method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/portal/TeleportTransition;newLevel()Lnet/minecraft/server/level/ServerLevel;"
			)
	)
	private ServerLevel getLevelFromDummy1(TeleportTransition teleportTransition) {
		if (DimensionChangeHelper.isDummy(teleportTransition)) {
			ResourceKey<Level> destinationWorldKey = DimensionChangeHelper.getDestinationFromNonPassengerDummyElseNull(teleportTransition);
			return ((MinecraftServerExtended) Objects.requireNonNull(this.level().getServer())).worldthreader$getLevelUnsynchronized(Objects.requireNonNull(destinationWorldKey));
		}
		return teleportTransition.newLevel();
	}

	@ModifyExpressionValue(
			method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/TeleportTransition;asPassenger()Z")
	)
	private boolean disallowCrossDimensionalPassengerTeleport(boolean isPassenger, @Local(argsOnly = true) TeleportTransition teleportTransition, @Local(ordinal = 0) boolean crossDimensional, @Local(ordinal = 1) ServerLevel destination) {
        if (crossDimensional && isPassenger && WorldThreadingManager.isWorldAccessDenied(destination)) {
			DimensionChangeHelper.expectDummy(teleportTransition);
		}
        return isPassenger;
    }


	@Unique
	private static final ThreadLocal<TeleportedEntityInfo> PASSENGER_TELEPORTED_ENTITY_INFO = new ThreadLocal<>();

	@WrapOperation(
			method = "teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;")
	)
	public Entity convertPassengersToTeleportedEntityInfos(Entity passenger, TeleportTransition teleportTransition, Operation<Entity> original, @Local(argsOnly = true) ServerLevel destination, @Share("PassengerInfos") LocalRef<List<TeleportedEntityInfo>> passengerInfos) {
		if (WorldThreadingManager.isWorldAccessDenied(destination)) {
			DimensionChangeHelper.expectDummy(teleportTransition);

			if (passengerInfos.get() == null) {
                passengerInfos.set(new ArrayList<>());
            }

			//This call should set PASSENGER_TELEPORTED_ENTITY_INFO
			var ret = original.call(passenger, teleportTransition);
			if (ret != null) {
                throw new IllegalStateException("Worldthreader: Teleportation was finished unexpectedly!");
            }
			TeleportedEntityInfo teleportedEntityInfo = PASSENGER_TELEPORTED_ENTITY_INFO.get();
            PASSENGER_TELEPORTED_ENTITY_INFO.remove();
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
		if (WorldThreadingManager.isWorldAccessDenied(destination)) {

			cir.setReturnValue(null);
			DimensionChangeHelper.expectDummy(teleportTransition);

			ResourceKey<Level> dest = DimensionChangeHelper.getDestinationFromNonPassengerDummyElseNull(teleportTransition);
			boolean isPassenger = dest == null;

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
				if (PASSENGER_TELEPORTED_ENTITY_INFO.get() != null) {
					throw new IllegalStateException("Worldthreader: Cannot store entity info as field is already set!");
				}
				PASSENGER_TELEPORTED_ENTITY_INFO.set(entityInfo);
			} else {
                ((ServerWorldExtended) destination).worldthreader$receiveTeleportedEntity((ServerLevel) this.level(), entityInfo);
            }
		}
	}


	//[VanillaCopy] Entity.copyFrom(Entity)
	@Unique
	private static CompoundTag copyFromToNBT(Entity original) {
        return original.saveWithoutId(new CompoundTag());
	}
}
