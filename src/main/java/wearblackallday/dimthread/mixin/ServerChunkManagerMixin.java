package wearblackallday.dimthread.mixin;

import wearblackallday.dimthread.DimThread;
import wearblackallday.dimthread.thread.IMutableMainThread;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerChunkManager.class, priority = 1001)
public abstract class ServerChunkManagerMixin extends ChunkManager implements IMutableMainThread {

	@Shadow @Final @Mutable private Thread serverThread;
	@Shadow @Final public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;
	@Shadow @Final private ServerWorld world;

	@Override
	public Thread getMainThread() {
		return this.serverThread;
	}

	@Override
	public void setMainThread(Thread thread) {
		this.serverThread = thread;
	}
}
