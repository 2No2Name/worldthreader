package no2.worldthreader.mixin.thread_ownership;

import no2.worldthreader.common.thread.ThreadOwnedObject;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.ChunkSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ServerChunkCache.class)
public abstract class ServerChunkManagerMixin extends ChunkSource implements ThreadOwnedObject {

    @Mutable
    @Shadow
    @Final
    Thread mainThread;

    @Override
    public Thread worldthreader$getOwningThread() {
        return this.mainThread;
    }

    @Override
    public void worldthreader$setOwningThread(Thread thread) {
        this.mainThread = thread;
    }
}
