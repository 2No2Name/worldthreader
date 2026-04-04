package no2.worldthreader.common.mixin_support.interfaces;

import net.minecraft.world.level.Level;

public interface EntityReferenceExtended<StoredEntityType> {
    StoredEntityType worldthreader$getEntitySameDimension(Level level, Class<StoredEntityType> clazz);

    boolean worldthreader$isEntityInSameDimension(Level level, Class<StoredEntityType> clazz);
}
