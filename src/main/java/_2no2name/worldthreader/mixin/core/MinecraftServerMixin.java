package _2no2name.worldthreader.mixin.core;

import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import _2no2name.worldthreader.common.thread.WorldThreadingManager;
import _2no2name.worldthreader.init.ModGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerExtended {

	private boolean shouldUseMultithreading = ModGameRules.INITIAL_TRUE;
	private WorldThreadingManager worldThreadingManager;

	@Shadow
	public abstract Iterable<ServerWorld> getWorlds();

	@Shadow
	protected abstract boolean shouldKeepTicking();

	private void replaceWorldThreadingManager() {
		if (this.worldThreadingManager != null) {
			this.worldThreadingManager.terminate();
			this.worldThreadingManager = null;
		}

		if (this.shouldUseMultithreading) {
			this.worldThreadingManager = new WorldThreadingManager((MinecraftServer) (Object) this);
		}
	}

	@Redirect(
			method = "tickWorlds",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorlds()Ljava/lang/Iterable;"),
			require = 1, allow = 1
	)
	private Iterable<ServerWorld> skipWorldLoop(MinecraftServer instance) {
		if (this.shouldUseMultithreading == !this.isThreadingEnabled()) {
			this.replaceWorldThreadingManager();
		}

		if (!this.isThreadingEnabled()) {
			return this.getWorlds();
		}

		//Start of tick barrier
		this.worldThreadingManager.setMultiThreadedPhase(true);
		this.worldThreadingManager.tickBarrier();

		//The world threads do work here in parallel


		//End of tick barrier
		this.worldThreadingManager.tickBarrier();
		this.worldThreadingManager.setMultiThreadedPhase(false);
		return Collections.emptyList();
	}

	@Override
	public boolean isTickMultithreaded() {
		return this.isThreadingEnabled() && this.worldThreadingManager.isMultiThreadedPhase();
	}

	/**
	 * Shutdown all threadpools when the server stop.
	 * Prevent server hang when stopping the server.
	 */
	@Inject(method = "shutdown", at = @At("HEAD"))
	public void shutdownThreading(CallbackInfo ci) {
		if (this.isThreadingEnabled()) {
			this.worldThreadingManager.terminate();
		}
	}

	private boolean isThreadingEnabled() {
		return this.worldThreadingManager != null;
	}

	@Override
	public void setThreadingEnabled(boolean value) {
		this.shouldUseMultithreading = value;
	}

	@Override
	public WorldThreadingManager getWorldThreadingManager() {
		return worldThreadingManager;
	}

	@Override
	public boolean shouldKeepTickingThreaded() {
		//TODO confirm this implementation does not cause issues (unclear semantics / missing memory visibility guarantees)
		return this.shouldKeepTicking();
	}
}
