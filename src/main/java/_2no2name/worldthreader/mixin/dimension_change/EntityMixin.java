package _2no2name.worldthreader.mixin.dimension_change;

import _2no2name.worldthreader.common.ServerWorldTicking;
import _2no2name.worldthreader.common.dimension_change.TeleportedEntityInfo;
import _2no2name.worldthreader.common.mixin_support.interfaces.EntityExtended;
import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import _2no2name.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import _2no2name.worldthreader.init.ModGameRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtended {

	@Shadow
	public World world;
	@Shadow
	protected BlockPos lastNetherPortalPosition;

	private static NbtCompound copyFromToNBT(Entity original) {
		NbtCompound nbtCompound = original.writeNbt(new NbtCompound());
		nbtCompound.remove("Dimension");
		nbtCompound.putLong("LastNetherPortalPosition", ((EntityMixin) (Object) original).lastNetherPortalPosition.asLong());
		return nbtCompound;
	}

	@Shadow
	protected @Nullable
	abstract TeleportTarget getTeleportTarget(ServerWorld destination);

	@Shadow
	public abstract EntityType<?> getType();

	@Shadow
	protected abstract void removeFromDimension();

	@Shadow
	public abstract void readNbt(NbtCompound nbt);

	@Shadow
	protected abstract Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect);

	//TODO this hides an inject from fabric-api (net.fabricmc.fabric.mixin.dimension.EntityMixin)
	@Inject(
			method = "moveToWorld",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;detach()V", shift = At.Shift.AFTER),
			cancellable = true
	)
	public void moveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
		if (!((MinecraftServerExtended) Objects.requireNonNull(this.world.getServer())).isTickMultithreaded()) {
			return;
		}
		cir.setReturnValue(null);


		// [VanillaCopy] Code from getTeleportTarget copied here so it is run on the correct thread.
		Direction.Axis portalAxis;
		Vec3d inPortalPos;
		BlockState blockState = this.world.getBlockState(this.lastNetherPortalPosition);
		if (blockState.contains(Properties.HORIZONTAL_AXIS)) {
			portalAxis = blockState.get(Properties.HORIZONTAL_AXIS);
			BlockLocating.Rectangle rectangle = BlockLocating.getLargestRectangle(this.lastNetherPortalPosition, portalAxis, 21, Direction.Axis.Y, 21, pos -> this.world.getBlockState(pos) == blockState);
			inPortalPos = this.positionInPortal(portalAxis, rectangle);
		} else {
			portalAxis = Direction.Axis.X;
			inPortalPos = new Vec3d(0.5, 0.0, 0.0);
		}

		// [VanillaCopy] partial content of moveToWorld method
		NbtCompound entityNBT = copyFromToNBT((Entity) (Object) this);
		this.removeFromDimension();
		((ServerWorld) this.world).resetIdleTimeout();

		((ServerWorldExtended) destination).receiveTeleportedEntity((Entity) (Object) this, entityNBT, (ServerWorld) this.world, portalAxis, inPortalPos);

		this.world.getProfiler().pop();
	}

	/**
	 * Fixes deadlock when an entity teleports using a nether portal to another dimension during the world tick.
	 * Without this the source world would be accessed when arriving in the destination world on the destination's thread.
	 * To prevent this the relative position in the source nether portal is calculated on departure and stored in the TeleportedEntityInfo.
	 * On arrival the precalculated values are used instead of accessing the source world.
	 */
	@SuppressWarnings("UnresolvedMixinReference")
	@Redirect(
			method = "method_30331(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/BlockLocating$Rectangle;)Lnet/minecraft/world/TeleportTarget;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;")
	)
	private BlockState avoidAccessingWrongWorld(World instance, BlockPos pos) {
		if (!((MinecraftServerExtended) Objects.requireNonNull(this.world.getServer())).isTickMultithreaded()) {
			return instance.getBlockState(pos);
		}
		//Code that is circumvented here was already evaluated in our moveToWorld method
		return Blocks.AIR.getDefaultState();
	}

	/**
	 * Fixes deadlock when an entity teleports using a nether portal to another dimension during the world tick.
	 * Without this the source world would be accessed when arriving in the destination world on the destination's thread.
	 * To prevent this the relative position in the source nether portal is calculated on departure and stored in the TeleportedEntityInfo.
	 * On arrival the precalculated values are used instead of accessing the source world.
	 */
	@SuppressWarnings("UnresolvedMixinReference")
	@ModifyArg(
			index = 2,
			method = "method_30331(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/BlockLocating$Rectangle;)Lnet/minecraft/world/TeleportTarget;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/NetherPortal;getNetherTeleportTarget(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/BlockLocating$Rectangle;Lnet/minecraft/util/math/Direction$Axis;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/world/TeleportTarget;")
	)
	private Direction.Axis restorePortalAxis(ServerWorld destination, BlockLocating.Rectangle portalRect, Direction.Axis portalAxis, Vec3d offset, Entity entity, Vec3d velocity, float yaw, float pitch) {
		if (!((MinecraftServerExtended) Objects.requireNonNull(this.world.getServer())).isTickMultithreaded()) {
			return portalAxis;
		}
		TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).getCurrentlyArrivingEntityInfo();
		return currentlyArrivingEntity.portalAxis();
	}

	/**
	 * Fixes deadlock when an entity teleports using a nether portal to another dimension during the world tick.
	 * Without this the source world would be accessed when arriving in the destination world on the destination's thread.
	 * To prevent this the relative position in the source nether portal is calculated on departure and stored in the TeleportedEntityInfo.
	 * On arrival the precalculated values are used instead of accessing the source world.
	 */
	@SuppressWarnings("UnresolvedMixinReference")
	@ModifyArg(
			index = 3,
			method = "method_30331(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/BlockLocating$Rectangle;)Lnet/minecraft/world/TeleportTarget;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/NetherPortal;getNetherTeleportTarget(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/BlockLocating$Rectangle;Lnet/minecraft/util/math/Direction$Axis;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/world/TeleportTarget;")
	)
	private Vec3d restoreInPortalPos(ServerWorld destination, BlockLocating.Rectangle portalRect, Direction.Axis portalAxis, Vec3d inPortalPos, Entity entity, Vec3d velocity, float yaw, float pitch) {
		if (!((MinecraftServerExtended) Objects.requireNonNull(this.world.getServer())).isTickMultithreaded()) {
			return inPortalPos;
		}
		TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) destination).getCurrentlyArrivingEntityInfo();
		return currentlyArrivingEntity.inPortalPos();
	}

	@Override
	public void arriveInWorld(NbtCompound entityNBT, ServerWorld destination) {
		// [VanillaCopy] partial content of moveToWorld method

		//This must be called from the thread of the destination world!
		//Note: The instance "this" is the removed instance from the other world. DO NOT USE IT FOR ANYTHING UNLESS ABSOLUTELY REQUIRED AND SAFE
		destination.getProfiler().push("reposition");
		TeleportTarget teleportTarget = this.getTeleportTarget(destination);
		if (teleportTarget == null) {
			return;
		}
		destination.getProfiler().swap("reloading");
		Entity entity = this.getType().create(destination);
		if (entity != null) {
			((EntityMixin) (Object) entity).copyFromNBT(entityNBT);
			entity.refreshPositionAndAngles(teleportTarget.position.x, teleportTarget.position.y, teleportTarget.position.z, teleportTarget.yaw, entity.getPitch());
			entity.setVelocity(teleportTarget.velocity);
			destination.onDimensionChanged(entity);
			if (destination.getRegistryKey() == World.END) {
				ServerWorld.createEndSpawnPlatform(destination);
			}
		}
		destination.resetIdleTimeout();
		destination.getProfiler().pop();

		if (entity != null) {
			((EntityExtended) entity).onArrivedInWorld();
		}

		if (entity != null && ModGameRules.SHOULD_TICK_ENTITY_AFTER_TELEPORT && ServerWorldTicking.isMainWorld(destination)) {
			entity.tick();
		}
	}

	public void copyFromNBT(NbtCompound nbtCompound) {
		this.readNbt(nbtCompound);
		this.lastNetherPortalPosition = BlockPos.fromLong(nbtCompound.getLong("LastNetherPortalPosition"));
	}
}
