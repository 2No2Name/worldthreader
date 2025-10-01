package no2.worldthreader.common.thread;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import no2.worldthreader.WorldThreaderMod;
import no2.worldthreader.common.ServerWorldTicking;
import no2.worldthreader.common.WorldThreaderTickPhase;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static no2.worldthreader.init.ModGameRules.INITIAL_FALSE;

public class WorldThreadingManager {

	public static boolean DEBUG = INITIAL_FALSE; //This should not be static, but it is used for debugging only

	private final MinecraftServer server;
	private final Phaser tickBarrier;
	private final Phaser withinTickBarrier;
	private final Reference2ReferenceLinkedOpenHashMap<Thread, ResourceKey<Level>> worldThreads;
	private final Reference2ReferenceOpenHashMap<Thread, ThreadOwnedObject[]> worldThreads2OwnedObjects;


	private final AtomicInteger threadsRequestingExclusiveWorldAccess = new AtomicInteger();
	private final Semaphore exclusiveWorldAccessLock = new Semaphore(1);
	//This variable is only modified by the owner of the permit from the semaphore (using the semaphore like a mutex)
	private final AtomicReference<Thread> threadWithExclusiveWorldAccess = new AtomicReference<>(null);

	private boolean isMultiThreadedPhase = false;
	private CrashReport crashReport;

    public final Object2ReferenceOpenHashMap<UUID, PlayerInfo> lastPlayerInfos = new Object2ReferenceOpenHashMap<>();



	public WorldThreadingManager(MinecraftServer server) {
		WorldThreaderMod.initializeBeforeThreading(server);

		this.server = server;
		this.tickBarrier = new Phaser();
		this.withinTickBarrier = new Phaser();
		this.tickBarrier.register();

		this.worldThreads = new Reference2ReferenceLinkedOpenHashMap<>();
		this.worldThreads2OwnedObjects = new Reference2ReferenceOpenHashMap<>();

		Iterable<ServerLevel> worlds = this.server.getAllLevels();
		for (ServerLevel world : worlds) {
			ThreadOwnedObject[] worldThreadOwned = {((ThreadOwnedObject) world), (ThreadOwnedObject) world.getChunkSource()};
			Thread worldThread = new Thread(() -> ServerWorldTicking.runWorldThread(server, this, world, worldThreadOwned));
			ThreadHelper.setWorldThreadName(worldThread, world);
			this.worldThreads2OwnedObjects.put(worldThread, worldThreadOwned);
			//Insert the worlds in ticking order
			this.worldThreads.put(worldThread, world.dimension());

            this.tickBarrier.register();
			this.withinTickBarrier.register();
			worldThread.start();
		}
	}

	public static void ensureExclusiveScoreboardAccess(MinecraftServer server) {
		WorldThreadingManager worldThreadingManager = ((MinecraftServerExtended) server).worldthreader$getThreadingManager();
		if (worldThreadingManager != null && worldThreadingManager.isMultiThreadedPhase()) {
            worldThreadingManager.waitForExclusiveWorldAccess(false);
        }
	}

	public static void crashIfNoExclusiveScoreboardAccess(MinecraftServer server) {
		if (!hasExclusiveScoreboardAccess(server)) {
			throw new IllegalStateException("Worldthreader: Scoreboard operation requires exclusive scoreboard access!");
        }
	}

	public static boolean hasExclusiveScoreboardAccess(MinecraftServer server) {
		WorldThreadingManager worldThreadingManager = ((MinecraftServerExtended) server).worldthreader$getThreadingManager();
		if (worldThreadingManager != null && worldThreadingManager.isMultiThreadedPhase()) {
			Thread currentThread = Thread.currentThread();
			Thread thread = worldThreadingManager.threadWithExclusiveWorldAccess.get();
			return thread == currentThread;
		}
		return true;
	}

	public static boolean isPlacingReceivedTeleports(ServerLevel newLevel) {
		return isMultithreadingAndCorrectThreadForWorld(newLevel) && ((ServerWorldExtended) newLevel).worldthreader$getTickPhase() == WorldThreaderTickPhase.RECEIVE_TELEPORTS;
	}
	public static boolean isRecoveringTeleports(ServerLevel newLevel) {
		return isMultithreadingAndCorrectThreadForWorld(newLevel) && ((ServerWorldExtended) newLevel).worldthreader$getTickPhase() == WorldThreaderTickPhase.RECOVER_FAILED_TELEPORTS;
	}

	public static WorldThreadingManager get(ServerLevel serverLevel) {
		return ((MinecraftServerExtended) serverLevel.getServer()).worldthreader$getThreadingManager();
	}

	public boolean isMultiThreadedPhase() {
		return this.isMultiThreadedPhase;
	}

	public void setMultiThreadedPhase(boolean value) {
		this.isMultiThreadedPhase = value;
	}

	public boolean isWorldThread(Thread thread) {
		return this.worldThreads.containsKey(thread);
	}

    public boolean isWorldThreadOf(ResourceKey<Level> dimension) {
		return dimension.equals(this.worldThreads.get(Thread.currentThread()));
	}

    public boolean isWorldThreadOf(ServerLevel serverLevel) {
		return this.isWorldThreadOf(serverLevel.dimension());
	}

	public static boolean isAccessibleForOtherThread(ServerLevel world) {
		return Thread.currentThread() != ((ThreadOwnedObject) world).worldthreader$getOwningThread();
	}

	public static boolean hasToAcquireExclusiveAccessBeforeAccessing(ServerLevel world) {
		return ((MinecraftServerExtended) world.getServer()).worldthreader$isTickMultithreaded() && WorldThreadingManager.isAccessibleForOtherThread(world);
	}

	public static boolean isWrongThreadForWorld(ServerLevel world) {
		WorldThreadingManager worldThreadingManager = ((MinecraftServerExtended) world.getServer()).worldthreader$getThreadingManager();
		return worldThreadingManager != null && worldThreadingManager.isMultiThreadedPhase() && !worldThreadingManager.isWorldThreadOf(world);
	}

	public static boolean isMultithreadingAndCorrectThreadForWorld(ServerLevel world) {
		WorldThreadingManager worldThreadingManager = ((MinecraftServerExtended) world.getServer()).worldthreader$getThreadingManager();
		return worldThreadingManager != null && worldThreadingManager.isMultiThreadedPhase() && worldThreadingManager.isWorldThreadOf(world);
	}

	public int tickBarrier() {
		return this.barrier(this.tickBarrier);
	}

	public void withinTickBarrier() {
		this.barrier(this.withinTickBarrier);
	}

	public boolean shouldKeepTickingThreaded() {
		return ((MinecraftServerExtended) this.server).worldthreader$shouldKeepTickingThreaded();
	}

	public void terminate() {
		this.tickBarrier.forceTermination();
		this.withinTickBarrier.forceTermination();
	}

	private int barrier(Phaser phaser) {
        boolean mustUnparkWaitingThread = this.tryGiveAwayExclusiveWorldAccess();
        int phase = phaser.getPhase();
        phaser.arrive();
        if (mustUnparkWaitingThread) {
            this.unparkThreadWaitingOnExclusiveWorldAccess();
        }
		return phaser.awaitAdvance(phase);
	}

	public boolean hasExclusiveWorldAccess() {
		return this.threadWithExclusiveWorldAccess.get() == Thread.currentThread();
	}

	/**
	 * For some reason the current thread (current ticking its world) wants to access another world.
	 * To guarantee some level of thread-safety, we need to wait until the thread of the other world is not modifying
	 * its world - meaning that it ran into a barrier or also entered this function.
	 * For now, acquiring exclusive access for all worlds at once. This can probably be changed, but then some
	 * threads will have to give away their exclusive access when requesting even more exclusive access.
	 * <p>
	 * Once exclusive world access is ensured, we can proceed. Releasing the exclusive world access is not possible
	 * until this thread runs into a barrier, because we cannot know for how long the thread is going to access the worlds.
	 * <p>
	 * Assumptions:
	 * After each barrier the threads will no longer access the other worlds until this function is called again.
	 */
    public void waitForExclusiveWorldAccess(boolean noDebug) {
		Thread currentThread = Thread.currentThread();
		Thread thread = this.threadWithExclusiveWorldAccess.get();

		if (!isWorldThread(currentThread)) {
			WorldThreaderMod.LOGGER.error("Thread {} is requesting exclusive world access. However, only world threads may request exclusive access during the world ticking!", currentThread);
			throw new IllegalStateException("Only world threads may request exclusive access during world ticking!");
		}

		if (DEBUG) {
			if (thread != currentThread) {
                WorldThreaderMod.LOGGER.info("Thread {} is requesting exclusive world access", currentThread);
				WorldThreaderMod.LOGGER.info("This slows down the game, but at least doesn't break it.");
				WorldThreaderMod.LOGGER.info("Current thread with exclusive world access: {}", thread);
			} else {
				WorldThreaderMod.LOGGER.info("Thread {} is using the exclusive world access again", currentThread);
			}
			WorldThreaderMod.LOGGER.info("Thread {} stacktrace for information:", currentThread);
			new Exception().printStackTrace();
		}
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
				this.setOwnershipOfAllThreadOwnedObjects(currentThread);
				if (DEBUG) {
					WorldThreaderMod.LOGGER.info("Thread {} has acquired exclusive world access", currentThread.getName());
					WorldThreaderMod.LOGGER.info("Total threads requesting exclusive world access: {}", this.threadsRequestingExclusiveWorldAccess.get());
				}
				return;
			} else {
				//Use parking instead of spin-locking for performance reasons
				LockSupport.park(this);
			}
		}
	}

	private void setOwnershipOfAllThreadOwnedObjects(Thread currentThread) {
		for (ThreadOwnedObject[] threadOwnedObjects : this.worldThreads2OwnedObjects.values()) {
			for (ThreadOwnedObject threadOwnedObject : threadOwnedObjects) {
				if (threadOwnedObject != null) {
					threadOwnedObject.worldthreader$setOwningThread(currentThread);
				}
			}
		}
    }

	private void resetOwnershipOfAllThreadOwnedObjects() {
        this.worldThreads2OwnedObjects.forEach((key, threadOwnedObjects) -> {
            for (ThreadOwnedObject threadOwnedObject : threadOwnedObjects) {
                if (threadOwnedObject != null) {
                    threadOwnedObject.worldthreader$setOwningThread(key);
                }
            }
        });
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

    public boolean tryGiveAwayExclusiveWorldAccess() {
		Thread thread = this.threadWithExclusiveWorldAccess.get();
		if (thread != null) {
			if (thread == Thread.currentThread()) {
				this.resetOwnershipOfAllThreadOwnedObjects();
				int andDecrement = this.threadsRequestingExclusiveWorldAccess.getAndDecrement();
				if (DEBUG) {
					WorldThreaderMod.LOGGER.info("Thread {} releasing exclusive world access", thread.getName());
					WorldThreaderMod.LOGGER.info("Other threads waiting for exclusive world access: {}", andDecrement - 1);
				}
				this.threadWithExclusiveWorldAccess.set(null);
				this.exclusiveWorldAccessLock.release();
			} else {
                return true; //Must unpark other thread after arriving in barrier
			}
		}
        return false;
    }

    public void unparkThreadWaitingOnExclusiveWorldAccess() {
        Thread thread = this.threadWithExclusiveWorldAccess.get();
        if (thread != null) {
            LockSupport.unpark(thread);
        }
    }

	public synchronized void handleCrash(CrashReport crashReport) {
		if (this.crashReport == null) {
			this.crashReport = crashReport;
			this.tickBarrier.forceTermination(); //Destroy the tick barrier to prevent all threads from entering a new tick and to wake up the main thread.
			//The main thread will call throwCrashReportIfPresent()
		} else {
			this.crashReport.addCategory("Crashing while already crashing").setDetail("Crash Report", crashReport);
		}
	}

	public void throwCrashIfPresent() {
		if (this.crashReport != null) {
			//Give all world threads the opportunity to finish gracefully.
			int threadsToJoin = this.worldThreads.size();
			for (int i = 0; i < 2 && threadsToJoin > 0; i++) {
				for (Thread thread : this.worldThreads.keySet()) {
					try {
						thread.join(1000);
						threadsToJoin--;
					} catch (InterruptedException ignored) {
					}
				}
				this.withinTickBarrier.forceTermination();
			}
			throw new ReportedException(this.crashReport);
		}
	}

    public void updateThreadsafePlayerInfos(Collection<ServerPlayer> players) {
        this.lastPlayerInfos.clear();
		for (ServerPlayer player : players) {
            this.lastPlayerInfos.put(player.getUUID(), new PlayerInfo(player));
        }
    }

    public record PlayerInfo(boolean dead, boolean removed, boolean wonGame) {
        public PlayerInfo(ServerPlayer player) {
            this(player.isDeadOrDying(), player.isRemoved(), player.wonGame);
        }
    }

    public boolean wasAlive(UUID uuid) {
        PlayerInfo playerInfo = this.lastPlayerInfos.get(uuid);
        return playerInfo == null || !playerInfo.dead() && !playerInfo.removed();
    }

    public boolean wasDead(UUID uuid) {
        PlayerInfo playerInfo = this.lastPlayerInfos.get(uuid);
        return playerInfo != null && playerInfo.dead();
    }


    public boolean wonGame(UUID uuid) {
        PlayerInfo playerInfo = this.lastPlayerInfos.get(uuid);
        return playerInfo != null && playerInfo.wonGame();
    }

}
