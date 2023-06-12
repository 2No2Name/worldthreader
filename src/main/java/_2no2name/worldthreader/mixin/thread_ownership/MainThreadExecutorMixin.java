package _2no2name.worldthreader.mixin.thread_ownership;

import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import _2no2name.worldthreader.common.thread.WorldThreadingManager;
import com.mojang.logging.LogUtils;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkManager.MainThreadExecutor.class)
public abstract class MainThreadExecutorMixin extends ThreadExecutor<Runnable> {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    private MinecraftServerExtended server;
    private boolean warned = false;

    protected MainThreadExecutorMixin(String name) {
        super(name);
    }

    @Shadow
    protected abstract Thread getThread();

    @Inject(
            method = "<init>", at = @At("RETURN")
    )
    private void captureWorld(ServerChunkManager serverChunkManager, World world, CallbackInfo ci) {
        this.server = (MinecraftServerExtended) world.getServer();
    }

    @Intrinsic
    @Override
    public void send(Runnable runnable) {
        super.send(runnable);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Inject(
            method = "send(Ljava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true
    )
    private void executeWithExclusiveWorldAccess(Runnable runnable, CallbackInfo ci) {
        if (this.server.isTickMultithreaded()) {
            Thread thread = Thread.currentThread();
            WorldThreadingManager worldThreadingManager = this.server.getWorldThreadingManager();
            if (thread != this.getThread()) {
                //noinspection ConstantConditions
                if (worldThreadingManager.isWorldThread(this.getThread())) {
                    if (worldThreadingManager.isWorldThread(thread)) {
                        if (!worldThreadingManager.hasExclusiveWorldAccess()) {
                            if (!this.warned) {
                                Exception exception = new Exception();
                                LOGGER.error("Worldthreader: A world thread (" + thread + ") is accessing another thread's (" + this.getThread() + ") world! Worldthreader tries its best to handle this, but this hints at a major mod compatibility issue which may corrupt your world! This warning is only given once per world thread! Please consider reporting this to the Worldthreader issue tracker! Stacktrace: ");
                                exception.printStackTrace();
                                this.warned = true;
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
