package no2.worldthreader.common.dimension_change;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record TeleportedEntityInfo(Entity oldEntityObject, CompoundTag nbtCompound, Direction.Axis portalAxis,
                                   Vec3 inPortalPos, List<TeleportedEntityInfo> passengers) {
}
