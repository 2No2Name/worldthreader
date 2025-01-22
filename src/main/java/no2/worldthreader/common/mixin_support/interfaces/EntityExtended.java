package no2.worldthreader.common.mixin_support.interfaces;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;

public interface EntityExtended {

    default void worldthreader$onArrivedInServerWorld(ResourceKey<Level> targetDimension, ResourceKey<Level> sourceDimension) {
    }

    /**
     * Called on the now removed entity. Required for some entities which act after removal (e.g. falling block entity for intentional end portal duplication).
     * For classes that override teleport and call super.teleport: Only needed for entities that react to the return value of teleport, which we change to null for cross dimension teleports.
     * <p>
     * For adding implementations, consider whether restoreEntity needs to be extended too!
     */
    default void worldthreader$onEntityDepartsFromServerWorld(ResourceKey<Level> targetDimension, ResourceKey<Level> sourceDimension) {
    }

    /**
     * Undo effects of failed teleportation attempts (Entity.removeAfterChangingDimensions, maybe others)
     * //Medium todo check whether passengers and vehicles are restored properly
     */
    void worldthreader$restoreEntity(TeleportedEntityInfo teleportedEntity);

    /**
     * Undo equipment effects of Entity.removeAfterChangingDimensions when a teleportation failed and the entity is sent back to its origin level
     */
    default void worldthreader$restoreEquipment(CompoundTag nbt) {
    }
}
