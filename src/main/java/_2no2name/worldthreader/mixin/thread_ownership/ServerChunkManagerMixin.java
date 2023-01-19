package _2no2name.worldthreader.mixin.thread_ownership;

import _2no2name.worldthreader.common.thread.IThreadOwnedObject;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.ChunkManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ServerChunkManager.class)
public abstract class ServerChunkManagerMixin extends ChunkManager implements IThreadOwnedObject {

    @Mutable
    @Shadow
    @Final
    Thread serverThread;

    @Override
    public Thread getOwningThread() {
        return this.serverThread;
    }

    @Override
    public void setOwningThread(Thread thread) {
        this.serverThread = thread;
    }
}
