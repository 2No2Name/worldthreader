package no2.worldthreader.mixin.core;


import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import no2.worldthreader.common.ServerWorldTicking;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {

    protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, bl, bl2, l, i);
    }

    @Shadow
    @NotNull
    public abstract MinecraftServer getServer();

    @Shadow protected abstract void advanceWeatherCycle();

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;advanceWeatherCycle()V"), method = "tick")
    private void tickWeatherThreaded(ServerLevel instance) {
        if (((MinecraftServerExtended) this.getServer()).worldthreader$isTickMultithreaded()) {
            this.tickWeatherThreaded();
        } else {
            this.advanceWeatherCycle();
        }
    }

    @Unique
    private void tickWeatherThreaded() {
        WorldThreadingManager worldThreadingManager = Objects.requireNonNull(((MinecraftServerExtended) ((ServerLevel) (Object) this).getServer()).worldthreader$getThreadingManager());
        boolean isMainWorld = ServerWorldTicking.isMainWorld((ServerLevel) (Object) this);
        if (!isMainWorld) {
            //Dependent worlds need to wait for the main world to update the weather first, otherwise they might update their weather based on
            //outdated values or values read with race conditions.
            worldThreadingManager.withinTickBarrier();
            this.advanceWeatherCycle(); //TODO multiple threads sending packets in here?
        } else {
            //Update the weather from the main world immediately
            this.advanceWeatherCycle();
            //The main world does not need to wait for dependent worlds ticking their weather
            worldThreadingManager.withinTickBarrier();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;updateSkyBrightness()V"), method = "tick")
    private void calcAmbientDarknessBeforeTickingTime(ServerLevel serverWorld) {
        if (((MinecraftServerExtended) this.getServer()).worldthreader$isTickMultithreaded()) {
            //Only calculate ambient darkness on the main world here, delay for other worlds.
            // Time ticks in the overworld before other dimensions tick their sky brightness
            boolean isMainWorld = ServerWorldTicking.isMainWorld((ServerLevel) (Object) this);
            if (isMainWorld) {
                //Update the weather from the main world immediately
                serverWorld.updateSkyBrightness();
            }
        } else {
            serverWorld.updateSkyBrightness();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tickTime()V", shift = At.Shift.AFTER), method = "tick")
    private void calcAmbientDarknessAfterTickingTime(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        MinecraftServerExtended server = (MinecraftServerExtended) this.getServer();
        if (server.worldthreader$isTickMultithreaded()) {
            WorldThreadingManager worldThreadingManager = Objects.requireNonNull(server.worldthreader$getThreadingManager());
            boolean isMainWorld = ServerWorldTicking.isMainWorld((ServerLevel) (Object) this);

            //The main world does not need to wait for dependent worlds ticking their weather or ambient darkness
            //Dependent worlds need to wait for the main world to update the time first, otherwise they might update their ambient darkness based on
            //outdated values or values read with race conditions.
            worldThreadingManager.withinTickBarrier();
            if (!isMainWorld) {
                this.updateSkyBrightness();
            }
        }
    }
}
