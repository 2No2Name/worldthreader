package no2.worldthreader.common.mixin_support.interfaces;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import org.jetbrains.annotations.Nullable;

public interface UnsafeOwnerAccess<T extends UniquelyIdentifyable> {
    @Nullable EntityReference<T> worldthreader$getCachedOwnerUnsafe();
}
