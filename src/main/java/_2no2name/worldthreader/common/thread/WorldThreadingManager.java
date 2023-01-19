package _2no2name.worldthreader.common.thread;

import _2no2name.worldthreader.common.ServerWorldTicking;
import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class WorldThreadingManager {

	private final MinecraftServer server;
	private final Phaser tickBarrier;
	private final Phaser withinTickBarrier;
	private final Reference2ReferenceLinkedOpenHashMap<Thread, ServerWorld> worldThreads;
//	private final ArrayList<AtomicBoolean> threadParallelWorldAccessPermitted;


	private final AtomicInteger threadsRequestingExclusiveWorldAccess = new AtomicInteger();
	private final Semaphore exclusiveWorldAccessLock = new Semaphore(1);
	//This variable is only modified by the owner of the permit from the semaphore (using the semaphore like a mutex)
	private final AtomicReference<Thread> threadWithExclusiveWorldAccess = new AtomicReference<>(null);

	private boolean isMultiThreadedPhase = false;


	public WorldThreadingManager(MinecraftServer server) {
		this.server = server;
		this.tickBarrier = new Phaser();
		this.withinTickBarrier = new Phaser();
		this.tickBarrier.register();

		this.worldThreads = new Reference2ReferenceLinkedOpenHashMap<>();

		Iterable<ServerWorld> worlds = this.server.getWorlds();
		for (ServerWorld world : worlds) {
			Thread worldThread = new Thread(() -> ServerWorldTicking.runWorldThread(server, this, world));
			ThreadHelper.attach(worldThread, world);
			//Insert the worlds in ticking order
			this.worldThreads.put(worldThread, world);

			this.tickBarrier.register();
			this.withinTickBarrier.register();
			worldThread.start();
		}
	}


	public boolean isMultiThreadedPhase() {
		return this.isMultiThreadedPhase;
	}

	public void setMultiThreadedPhase(boolean value) {
		this.isMultiThreadedPhase = value;
	}

	public Reference2ReferenceLinkedOpenHashMap<Thread, ServerWorld> getWorldThreads() {
		return this.worldThreads;
	}

	public int tickBarrier() {
		return this.barrier(this.tickBarrier);
	}

	public void withinTickBarrier() {
		this.barrier(this.withinTickBarrier);
	}

	public boolean shouldKeepTickingThreaded() {
		return ((MinecraftServerExtended) this.server).shouldKeepTickingThreaded();
	}

	public void terminate() {
		this.tickBarrier.forceTermination();
		this.withinTickBarrier.forceTermination();
	}

	private int barrier(Phaser phaser) {
		int phase = phaser.getPhase();
		phaser.arrive();
		this.tryGiveAwayExclusiveWorldAccess();
		return phaser.awaitAdvance(phase);
	}

	/**
	 * For some reason the current thread (current ticking its world) wants to access another world.
	 * To guarantee some level of thread-safety, we need to wait until the thread of the other world is not modifying
	 * its world - meaning that it ran into a barrier or also entered this function.
	 * <p>
	 * Once exclusive world access is ensured, we can proceed. Releasing the exclusive world access is not possible
	 * until this thread runs into a barrier, because we cannot know for how long the thread is going to access the worlds.
	 * <p>
	 * Assumptions:
	 * After each barrier the threads will no longer access the other worlds until this function is called again.
	 */
	public void waitForExclusiveWorldAccess() {
		Thread currentThread = Thread.currentThread();
		Thread thread = this.threadWithExclusiveWorldAccess.get();
		if (thread == currentThread) {
			return;
		}

		this.threadsRequestingExclusiveWorldAccess.getAndIncrement();
		thread = this.threadWithExclusiveWorldAccess.get();
		if (thread != null) {
			LockSupport.unpark(thread);
		}

		//If multiple threads try to acquire exclusive world access, all but one will block here
		this.exclusiveWorldAccessLock.acquireUninterruptibly();


		this.threadWithExclusiveWorldAccess.set(currentThread);

		while (true) {
			boolean allOtherThreadsWaiting = this.areAllThreadsInBarrierOrAccessRequest();
			if (allOtherThreadsWaiting) {
				//Now we have exclusive world access.
				return;
			} else {
				//Use parking instead of spinlocking for performance reasons
				LockSupport.park(this);
			}
		}
	}

	private boolean areAllThreadsInBarrierOrAccessRequest() {
		int totalThreads = this.tickBarrier.getRegisteredParties();

		int arrivedParties = this.withinTickBarrier.getRegisteredParties() - this.withinTickBarrier.getUnarrivedParties();
		arrivedParties += this.tickBarrier.getRegisteredParties() - this.tickBarrier.getUnarrivedParties();
		arrivedParties += this.threadsRequestingExclusiveWorldAccess.get();

		if (arrivedParties > totalThreads) {
			throw new IllegalStateException("More arrived parties than expected!");
		}

		return totalThreads == arrivedParties;
	}

	private void tryGiveAwayExclusiveWorldAccess() {
		Thread thread = this.threadWithExclusiveWorldAccess.get();
		if (thread != null) {
			if (thread == Thread.currentThread()) {
				this.threadsRequestingExclusiveWorldAccess.getAndDecrement();
				this.threadWithExclusiveWorldAccess.set(null);
				this.exclusiveWorldAccessLock.release();
			} else {
				LockSupport.unpark(thread);
			}
		}
	}
}
