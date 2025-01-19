package no2.worldthreader.mixin.thread_ownership;

import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkCache.MainThreadExecutor.class)
public abstract class MainThreadExecutorMixin extends BlockableEventLoop<Runnable> {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    @Unique
    private MinecraftServerExtended server;
    @Unique
    private boolean warned = false;

    protected MainThreadExecutorMixin(String name) {
        super(name);
    }

    @Shadow
    protected abstract Thread getRunningThread();

    @Inject(
            method = "<init>", at = @At("RETURN")
    )
    private void captureServer(ServerChunkCache serverChunkManager, Level world, CallbackInfo ci) {
        this.server = (MinecraftServerExtended) world.getServer();
    }

    @Intrinsic
    @Override
    public void schedule(Runnable runnable) {
        super.schedule(runnable);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Inject(
            method = "schedule(Ljava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true
    )
    private void executeWithExclusiveWorldAccess(Runnable runnable, CallbackInfo ci) {
        if (this.server.worldthreader$isTickMultithreaded()) {
            Thread thread = Thread.currentThread();
            WorldThreadingManager worldThreadingManager = this.server.worldthreader$getThreadingManager();
            if (thread != this.getRunningThread()) {
                //noinspection ConstantConditions
                if (worldThreadingManager.isWorldThread(this.getRunningThread())) {
                    if (worldThreadingManager.isWorldThread(thread)) {
                        if (!worldThreadingManager.hasExclusiveWorldAccess()) {
                            if (!this.warned) {
                                IllegalStateException exception = new IllegalStateException("Worldthreader: Cross-World Access Detected");
                                LOGGER.error("Worldthreader: A world thread (" + thread + ") is accessing another thread's (" + this.getRunningThread() + ") world! Worldthreader tries its best to handle this, but this hints at a major mod compatibility issue which may corrupt your world! This warning is only given once per world thread! Please consider reporting this to the Worldthreader issue tracker! Stacktrace: ");
                                exception.printStackTrace();
                                this.warned = true;
                                throw exception; //TODO remove this line
                            }
                            worldThreadingManager.waitForExclusiveWorldAccess();
                        }
                        runnable.run();
                        ci.cancel();
                    }
                }
            }
        }
    }
}
