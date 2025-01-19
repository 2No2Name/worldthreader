package no2.worldthreader.mixin.core;

import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import no2.worldthreader.init.ModGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerExtended {

	@Shadow protected abstract boolean haveTime();

	@Shadow public abstract Iterable<ServerLevel> getAllLevels();

	@Unique
	private boolean shouldUseMultithreading = ModGameRules.INITIAL_TRUE;
	@Unique
	private WorldThreadingManager worldThreadingManager;

	@Unique
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
			method = "tickChildren(Ljava/util/function/BooleanSupplier;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"),
			require = 1, allow = 1
	)
	private Iterable<ServerLevel> multiThreadWorldLoop(MinecraftServer instance) {
		if (this.shouldUseMultithreading == !this.isThreadingEnabled()) {
			this.replaceWorldThreadingManager();
		}

		if (!this.isThreadingEnabled()) {
			return this.getAllLevels();
		}

		//Start of tick barrier
		this.worldThreadingManager.setMultiThreadedPhase(true);
		this.worldThreadingManager.tickBarrier();

		//The world threads do work here in parallel
		// See Redirect target

		//End of tick barrier
		this.worldThreadingManager.tickBarrier();
		this.worldThreadingManager.setMultiThreadedPhase(false);

		this.worldThreadingManager.throwCrashIfPresent();

		return Collections.emptyList();
	}

	@Override
	public boolean worldthreader$isTickMultithreaded() {
		return this.isThreadingEnabled() && this.worldThreadingManager.isMultiThreadedPhase();
	}

	/**
	 * Shutdown all world threads when the server stops.
	 * Prevent server hang when stopping the server.
	 */
	@Inject(method = "stopServer()V", at = @At("HEAD"))
	public void shutdownThreading(CallbackInfo ci) {
		if (this.isThreadingEnabled()) {
			this.worldThreadingManager.terminate();
		}
	}

	@Unique
	private boolean isThreadingEnabled() {
		return this.worldThreadingManager != null;
	}

	@Override
	public void worldthreader$setThreadingEnabled(boolean value) {
		this.shouldUseMultithreading = value;
	}

	@Override
	public WorldThreadingManager worldthreader$getThreadingManager() {
		return this.worldThreadingManager;
	}

	@Override
	public boolean worldthreader$shouldKeepTickingThreaded() {
		//TODO confirm this implementation does not cause issues (unclear semantics / missing memory visibility guarantees)
		return this.haveTime();
	}
}
