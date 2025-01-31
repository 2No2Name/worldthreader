package no2.worldthreader.mixin.debug;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import no2.worldthreader.WorldThreaderMod;
import no2.worldthreader.common.thread.ThreadOwnedObject;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;

@Mixin(value = ServerChunkCache.class, priority = 1010)
//priority is set to 1010 to ensure that this mixin is applied after the getChunk overwrite in ServerChunkCacheMixin from Lithium
public abstract class ServerChunkCacheMixin implements ThreadOwnedObject {

    @SuppressWarnings("ShadowModifiers")
    @Shadow
    @Final
    public Thread mainThread;

    @Shadow
    @Final
    public ChunkMap chunkMap;

    @WrapMethod(
            method = "getChunkFuture"
    )
    private CompletableFuture<ChunkResult<ChunkAccess>> debugThreadSafety0(int i, int j, ChunkStatus chunkStatus, boolean bl, Operation<CompletableFuture<ChunkResult<ChunkAccess>>> original) {
        debugThreadSafety(i, j, chunkStatus);
        return original.call(i, j, chunkStatus, bl);
    }

    @WrapMethod(
            method = "getChunk"
    )
    private ChunkAccess debugThreadSafety1(int i, int j, ChunkStatus chunkStatus, boolean bl, Operation<ChunkAccess> original) {
        debugThreadSafety(i, j, chunkStatus);
        return original.call(i, j, chunkStatus, bl);
    }

    @Unique
    private void debugThreadSafety(int i, int j, ChunkStatus chunkStatus) {
        if (WorldThreadingManager.DEBUG && chunkStatus != ChunkStatus.FULL) { //For some reason some chunk generation stuff calls the ServerChunkCache (e.g. placing a generated cat in a village)
            Thread currentThread = Thread.currentThread();
            if (this.mainThread != currentThread) {
                WorldThreaderMod.LOGGER.error("Thread {} is illegally accessing a chunk ({},{}) from ServerChunkCache owned by thread {}!", currentThread, i, j, this.worldthreader$getOwningThread());
                WorldThreaderMod.LOGGER.error("This breaks the game.");
                WorldThreaderMod.LOGGER.error("Thread {} stacktrace:", currentThread);
                new Exception().printStackTrace();
            }
        }
    }
}
