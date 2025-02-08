package no2.worldthreader.common.mixin_support.interfaces;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.Nullable;

public interface MinecraftServerExtended {
    boolean worldthreader$isTickMultithreaded();

    @Nullable
    WorldThreadingManager worldthreader$getThreadingManager();

    boolean worldthreader$shouldKeepTickingThreaded();

    ServerLevel worldthreader$getLevelUnsynchronized(ResourceKey<Level> key);

}
