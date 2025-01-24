package no2.worldthreader.mixin.debug;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import no2.worldthreader.WorldThreaderMod;
import no2.worldthreader.common.thread.ThreadOwnedObject;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin implements ThreadOwnedObject {

    @SuppressWarnings("ShadowModifiers")
    @Shadow
    @Final
    public Thread mainThread;

    @Inject(
            method = {"getChunk", "getChunkFuture"}, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;")
    )
    private void bruh(int i, int j, ChunkStatus chunkStatus, boolean bl, CallbackInfoReturnable<ChunkAccess> cir) {
        Thread currentThread = Thread.currentThread();

        if (WorldThreadingManager.DEBUG) {
            if (this.mainThread != currentThread) {
                WorldThreaderMod.LOGGER.error("Thread {} is illegally accessing a chunk from ServerChunkCache owned by thread {}!", currentThread, this.worldthreader$getOwningThread());
                WorldThreaderMod.LOGGER.error("This breaks the game.");
                WorldThreaderMod.LOGGER.error("Thread {} stacktrace:", currentThread);
                new Exception().printStackTrace();
            }
        }
    }
}
