package _2no2name.worldthreader.common.mixin_support.interfaces;

import _2no2name.worldthreader.common.thread.WorldThreadingManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface MinecraftServerExtended {
    boolean isTickMultithreaded();

    @Nullable
    WorldThreadingManager getWorldThreadingManager();

    void setThreadingEnabled(boolean value);

    boolean shouldKeepTickingThreaded();

    ServerWorld getWorldUnsynchronized(RegistryKey<World> key);

}
