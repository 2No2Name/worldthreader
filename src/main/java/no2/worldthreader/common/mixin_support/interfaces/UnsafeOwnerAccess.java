package no2.worldthreader.common.mixin_support.interfaces;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface UnsafeOwnerAccess {
    @Nullable
    Entity worldthreader$getCachedOwnerUnsafe();
}
