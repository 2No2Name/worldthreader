package no2.worldthreader.common.mixin_support.interfaces;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.timers.TimerQueue;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

public interface MinecraftServerExtended {
    @Unique
    void worldthreader$onLevelAddedOrRemoved();

    boolean worldthreader$isTickMultithreaded();

    @Nullable
    WorldThreadingManager worldthreader$getThreadingManager();

    boolean worldthreader$shouldKeepTickingThreaded();

    ServerLevel worldthreader$getLevelUnsynchronized(ResourceKey<Level> key);

    TimerQueue<MinecraftServer> worldthreader$getScheduledEventsUnsafe();
}
