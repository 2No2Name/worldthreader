package _2no2name.worldthreader.mixin.profiler;

import _2no2name.worldthreader.common.ServerWorldTicking;
import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Shadow
    public abstract @NotNull MinecraftServer getServer();


    /**
     * Only profile on the main world for now, as the profiler is not threadsafe
     */
    @Override
    public Profiler getProfiler() {
        if (((MinecraftServerExtended) this.getServer()).isTickMultithreaded()) {
            if (!ServerWorldTicking.isMainWorld((ServerWorld) (Object) this)) {
                return DummyProfiler.INSTANCE;
            }
        }
        return super.getProfiler();
    }

    /**
     * Only profile on the main world for now, as the profiler is not threadsafe
     */
    @Override
    public Supplier<Profiler> getProfilerSupplier() {
        return this::getProfiler;
    }
}
