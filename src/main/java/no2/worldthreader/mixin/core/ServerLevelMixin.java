package no2.worldthreader.mixin.core;


import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.level.timers.TimerQueue;
import no2.worldthreader.common.ServerWorldTicking;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
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


    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/TickRateManager;runsNormally()Z"
            ),
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;tick()V"))
    )
    private void threadSafetyForPretickTimeWeatherAndSleeping(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        MinecraftServerExtended server = (MinecraftServerExtended) this.getServer();
        if (server.worldthreader$isTickMultithreaded()) {
            WorldThreadingManager worldThreadingManager = Objects.requireNonNull(server.worldthreader$getThreadingManager());
            boolean isMainWorld = ServerWorldTicking.isMainWorld((ServerLevel) (Object) this);

            //On vanilla, the main world (overworld) is updated first.
            //Update on the main world first (writes to shared data), followed by the other worlds
            if (!isMainWorld) {
                worldThreadingManager.withinTickBarrier();
            }
        }
    }

    @Redirect(
            method = "tickTime",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getScheduledEvents()Lnet/minecraft/world/level/timers/TimerQueue;"
            )
    )
    private TimerQueue<MinecraftServer> getScheduledEventsUnsafe(MinecraftServer instance) {
        if (ServerWorldTicking.isMainWorld((ServerLevel) (Object) this)) {
            //Main world runs this code with implicit exclusive access, see mixins above and below
            return ((MinecraftServerExtended) instance).worldthreader$getScheduledEventsUnsafe();
        }
        return instance.getScheduledEvents();
    }

    @Inject(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V"
            ),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tickTime()V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isDebug()Z")
            )
    )
    private void threadSafety2ForPretickTimeWeatherAndSleeping(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        MinecraftServerExtended server = (MinecraftServerExtended) this.getServer();
        if (server.worldthreader$isTickMultithreaded()) {
            WorldThreadingManager worldThreadingManager = Objects.requireNonNull(server.worldthreader$getThreadingManager());
            boolean isMainWorld = ServerWorldTicking.isMainWorld((ServerLevel) (Object) this);

            //Update on the main world first (writes to shared data), followed by the other worlds
            if (isMainWorld) {
                worldThreadingManager.withinTickBarrier();
            }
        }
    }
}
