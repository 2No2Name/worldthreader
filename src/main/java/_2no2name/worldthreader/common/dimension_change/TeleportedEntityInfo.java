package _2no2name.worldthreader.common.dimension_change;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public record TeleportedEntityInfo(Entity oldEntityObject, NbtCompound nbtCompound, Direction.Axis portalAxis,
                                   Vec3d inPortalPos) {
}
