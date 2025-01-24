package no2.worldthreader.common.mixin_support.interfaces;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.timers.TimerQueue;

public interface PrimaryLevelDataExtended {

    TimerQueue<MinecraftServer> worldthreader$getScheduledEventsUnsafe();
}
