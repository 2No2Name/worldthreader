package no2.worldthreader.mixin.safepoints;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {

    protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Shadow
    public abstract MinecraftServer getServer();

    @Inject(
            method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V")
    )
    private void safePointAtProfilerPush(BooleanSupplier haveTime, CallbackInfo ci) {
        this.safePoint();
    }

    @Inject(
            method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V")
    )
    private void safePointAtProfilerPopPush(BooleanSupplier haveTime, CallbackInfo ci) {
        this.safePoint();
    }

    @Inject(
            method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V")
    )
    private void safePointAtProfilerPop(BooleanSupplier haveTime, CallbackInfo ci) {
        this.safePoint();
    }

    @Inject(
            method = "lambda$tick$0", at = @At(value = "HEAD")
    )
    private void safePointBeforeEntityTick(TickRateManager tickRateManager, ProfilerFiller profiler, Entity entity, CallbackInfo ci) {
        this.safePoint();
    }

    @Unique
    private void safePoint() {
        WorldThreadingManager worldThreadingManager = WorldThreadingManager.get(this);
        worldThreadingManager.threadingSafePoint();
    }
}
