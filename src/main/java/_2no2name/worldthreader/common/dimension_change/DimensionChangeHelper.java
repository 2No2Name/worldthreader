package _2no2name.worldthreader.common.dimension_change;

import _2no2name.worldthreader.common.ServerWorldTicking;
import _2no2name.worldthreader.common.mixin_support.interfaces.EntityExtended;
import _2no2name.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import _2no2name.worldthreader.init.ModGameRules;
import _2no2name.worldthreader.mixin.dimension_change.EntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public class DimensionChangeHelper {
    public static void arriveInWorld(TeleportedEntityInfo teleportedEntityInfo, Entity staleEntity, NbtCompound entityNBT, EntityType<?> entityType, ServerWorld destination, ServerWorld source) {
        // [VanillaCopy] partial content of moveToWorld method

        //This must be called from the thread of the destination world!
        //Note: The instance "this" is the removed instance from the other world. DO NOT USE IT FOR ANYTHING UNLESS ABSOLUTELY REQUIRED AND SAFE
        destination.getProfiler().push("reposition");
        //Non-vanilla feature / compromise: Nether portals are created for all entities when using multithreading.
        //Aborting the teleport or sending entities back is a bad option
        //This is implemented in EntityMixin behind a gamerule.
        TeleportTarget teleportTarget = ((EntityAccessor) staleEntity).invokeGetTeleportTarget(destination);
        if (teleportTarget == null) {
            //Creating a portal was not possible or failed. We have to send the entity back. Note: The entity was
            //removed from the source world already and the world kept ticking until the end of the current tick!
            ((ServerWorldExtended) source).receiveFailedTeleport(teleportedEntityInfo);
            return;
        }

        destination.getProfiler().swap("reloading");
        Entity entity = entityType.create(destination);
        if (entity != null) {
            ((EntityExtended) entity).copyFromNBT(entityNBT);
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

    public static void restoreEntityInWorld(TeleportedEntityInfo entityInfo) {
        Entity entity = entityInfo.staleEntityObject();
        ((EntityExtended) entity).restoreEntity(entityInfo);
        ((ServerWorld) entity.getWorld()).tryLoadEntity(entity);
    }
}
