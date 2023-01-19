package _2no2name.worldthreader.common.mixin_support.interfaces;

import _2no2name.worldthreader.common.dimension_change.TeleportedEntityInfo;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface ServerWorldExtended {

    void receiveTeleportedEntity(Entity entity, NbtCompound entityNBT, ServerWorld source, Direction.Axis portalAxis, Vec3d inPortalPos);

    void finishReceivingTeleportedEntities();

    TeleportedEntityInfo getCurrentlyArrivingEntityInfo();
}
