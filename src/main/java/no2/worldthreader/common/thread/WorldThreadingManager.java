package no2.worldthreader.common.thread;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import no2.worldthreader.WorldThreaderMod;
import no2.worldthreader.common.ServerWorldTicking;
import no2.worldthreader.common.WorldThreaderTickPhase;
import no2.worldthreader.common.interdimensional.InterdimensionalEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static no2.worldthreader.init.ModGameRules.INITIAL_FALSE;

public class WorldThreadingManager {

	public static boolean DEBUG = INITIAL_FALSE; //This should not be static, but it is used for debugging only

	private final MinecraftServer server;
	private final Phaser tickBarrier;
	private final Phaser withinTickBarrier;
	private final Reference2ReferenceLinkedOpenHashMap<Thread, ResourceKey<Level>> worldThreads;
	private final Reference2ReferenceOpenHashMap<Thread, ThreadOwnedObject[]> worldThreads2OwnedObjects;


	private final AtomicInteger threadsRequestingExclusiveWorldAccess = new AtomicInteger();

	private final Semaphore yieldingLevels = new Semaphore(0); //Usually 0, unless threads are currently trying to give the world to another thread which hasn't acquired it yet
	private final Semaphore reacquireLevels = new Semaphore(0); //Usually 0, unless a thread is currently giving back its exclusive world access to the world threads


	//This variable is only modified by the owner of the permit from the semaphore (using the semaphore like a mutex)
	private final AtomicReference<Thread> threadWithExclusiveWorldAccess = new AtomicReference<>(null);

	private boolean isMultiThreadedPhase = false;
	private CrashReport crashReport;

    public final Object2ReferenceOpenHashMap<UUID, PlayerInfo> lastPlayerInfos = new Object2ReferenceOpenHashMap<>();
    private InterdimensionalEntityInfo interdimensionalEntityInfo;


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

	public int numberOfLevels() {
		return this.worldThreads.size();
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

	public static WorldThreadingManager get(Level level) {
		if (level instanceof ServerLevel serverLevel) {
			return ((MinecraftServerExtended) serverLevel.getServer()).worldthreader$getThreadingManager();
		}
		throw new IllegalArgumentException("Expected ServerLevel as argument!");
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

	public void threadingSafePoint() {
		this.tryGiveAwayExclusiveWorldAccess();
		if (this.threadsRequestingExclusiveWorldAccess.get() > 0) {
			this.releaseLevel();
			this.reacquireLevel();
		}
	}

	public void releaseLevel() {
		if (!this.isWorldThread(Thread.currentThread())) {
			throw new IllegalCallerException("Only level threads can release their level for other threads!");
		}
		this.yieldingLevels.release();
	}

	private void reacquireLevel() {
		if (!this.isWorldThread(Thread.currentThread())) {
			throw new IllegalCallerException("Only level threads can release reacquire their level after yielding!");
		}
		if (this.threadsRequestingExclusiveWorldAccess.get() == 0) {
			boolean b = this.yieldingLevels.tryAcquire();
			if (b) {
				return;
			}
			//If tryAcquire didn't work, it is guaranteed that we can reacquire below, since some other thread must have taken exclusive access
		}
		this.reacquireLevels.acquireUninterruptibly();
	}

	/**
	 * Barrier, also includes a safe point, see {@link WorldThreadingManager#threadingSafePoint()}
	 *
	 * @param phaser the phaser used
	 * @return next phaser phase, negative if terminated
	 */
	private int barrier(Phaser phaser) {
        this.tryGiveAwayExclusiveWorldAccess();
		boolean threadOwnsLevel = this.isWorldThread(Thread.currentThread());
		if (threadOwnsLevel) {
			this.releaseLevel();
		}
        int phase = phaser.getPhase();
		phaser.arrive();
		int nextPhase = phaser.awaitAdvance(phase);
		if (threadOwnsLevel) {
			this.reacquireLevel();
		}
		return nextPhase;
	}

	/**
	 * For some reason a current thread wants to access a world that is not its own.
	 * To guarantee some level of thread-safety, we need to wait until the thread of the other world is not modifying
	 * its world - meaning that it ran into a barrier or also entered this function.
	 * For now, acquiring exclusive access for all worlds at once. This can probably be changed, but then some
	 * threads will have to give away their exclusive access when requesting even more exclusive access. Also, deadlocks
	 * may be possible unless requesting more exclusive access includes releasing all held exclusive access.
	 * <p>
	 * Once exclusive world access is ensured, we can proceed. Releasing the exclusive world access is not possible
	 * until this thread runs into a safe point, because we cannot know for how long the thread is going to access the worlds.
	 * <p>
	 * Assumptions:
	 * After each safe point the threads will no longer access the other worlds until this function is called again.
	 */
    public void waitForExclusiveWorldAccess(boolean noDebug) {
		Thread currentThread = Thread.currentThread();
		Thread thread = this.threadWithExclusiveWorldAccess.get();

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
		if (this.isWorldThread(currentThread)) {
			this.yieldingLevels.release(); //Release our own level first to avoid deadlocks
		}
		//In case other threads have not reacquired their level (e.g. due to being in a barrier), we have to acquire
		// those as well.
		int toAcquire = this.numberOfLevels();
		//Non-atomic acquiring is fine here, since threadsRequestingExclusiveWorldAccess != 0 is ensured here, and other
		// threads only release to the reacquireLevels semaphore when threadsRequestingExclusiveWorldAccess == 0
		int availableReacquirePermits = this.reacquireLevels.availablePermits();
		if (availableReacquirePermits > 0)
			if (this.reacquireLevels.tryAcquire(availableReacquirePermits)) {
				toAcquire -= availableReacquirePermits;
			}

		if (toAcquire > 0) {
			//If multiple threads try to acquire exclusive world access, all but one will block here
			this.yieldingLevels.acquireUninterruptibly(toAcquire); //Also have to acquire our own level after releasing before
		}


		this.threadWithExclusiveWorldAccess.set(currentThread);

		//Now we have exclusive world access.
		this.setOwnershipOfAllThreadOwnedObjects(currentThread);
		if (DEBUG) {
			WorldThreaderMod.LOGGER.info("Thread {} has acquired exclusive world access", currentThread.getName());
			WorldThreaderMod.LOGGER.info("Total threads requesting exclusive world access: {}", this.threadsRequestingExclusiveWorldAccess.get());
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

    public void tryGiveAwayExclusiveWorldAccess() {
		Thread thread = this.threadWithExclusiveWorldAccess.get();
		if (thread != null && thread == Thread.currentThread()) {
			this.resetOwnershipOfAllThreadOwnedObjects();
			int threadsRequestingExclusiveAccess = this.threadsRequestingExclusiveWorldAccess.decrementAndGet();
			if (DEBUG) {
				WorldThreaderMod.LOGGER.info("Thread {} releasing exclusive world access", thread.getName());
				WorldThreaderMod.LOGGER.info("Other threads waiting for exclusive world access: {}", threadsRequestingExclusiveAccess);
			}
			this.threadWithExclusiveWorldAccess.set(null);
			int levelsToRelease = this.numberOfLevels();

			if (threadsRequestingExclusiveAccess == 0) {
				if (this.isWorldThread(thread)) {
					levelsToRelease--;
				}
				//Give back levels to the level threads
				this.reacquireLevels.release(levelsToRelease);
			} else {
				//Yield all levels again to allow waiting thread to progress
				this.yieldingLevels.release(levelsToRelease);
				if (this.isWorldThread(thread)) {
					this.reacquireLevel();
				}
			}
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
			System.err.println("Server crash report created. Giving threads a chance to finish gracefully...");
			//Give all world threads the opportunity to finish gracefully.
			this.worldThreads.keySet().removeIf(thread -> {
				try {
					thread.join(1000);
				} catch (InterruptedException ignored) {

				}
				return !thread.isAlive();
			});
			this.withinTickBarrier.forceTermination();
			this.worldThreads.keySet().removeIf(thread -> {
				try {
					thread.join(1000);
				} catch (InterruptedException ignored) {

				}
				return !thread.isAlive();
			});

			if (!this.worldThreads.isEmpty()) {
				CrashReportCategory levelThreadsNotFinishedGracefully = this.crashReport.addCategory("Level threads not finished gracefully");
				this.worldThreads.forEach((thread, level) -> {
					//Append to crash report
					levelThreadsNotFinishedGracefully.setDetail("Level", level);
					levelThreadsNotFinishedGracefully.setDetail("Thread", thread);
					levelThreadsNotFinishedGracefully.setDetail("Stacktrace", thread.getStackTrace());
				});
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

    public void updateThreadsafeUUIDInfos(Iterable<ServerLevel> allLevels) {
        this.interdimensionalEntityInfo = new InterdimensionalEntityInfo(allLevels);
    }

    public ServerLevel getUUIDLevel(UUID uUID, ServerLevel except) {
        for (Reference2ReferenceMap.Entry<ServerLevel, Set<UUID>> pair : this.interdimensionalEntityInfo.existingEntities().reference2ReferenceEntrySet()) {
            if (except != pair.getKey() && pair.getValue().contains(uUID)) {
                return pair.getKey();
            }
        }
        return null;
    }

	public record PlayerInfo(boolean dead, boolean removed, boolean wonGame,
							 GameType gameMode) {
        public PlayerInfo(ServerPlayer player) {
			this(player.isDeadOrDying(), player.isRemoved(), player.wonGame, player.gameMode());
        }
    }

	public boolean wasUUIDAPlayer(UUID uuid) {
		return this.lastPlayerInfos.containsKey(uuid);
	}

    public boolean wasPlayerAlive(UUID uuid, boolean fallback) {
        PlayerInfo playerInfo = this.lastPlayerInfos.get(uuid);
        return playerInfo == null ? fallback : !playerInfo.dead() && !playerInfo.removed();
    }

    public boolean wasPlayerDead(UUID uuid) {
        PlayerInfo playerInfo = this.lastPlayerInfos.get(uuid);
        return playerInfo != null && playerInfo.dead();
    }


    public boolean wasPlayerWonGame(UUID uuid) {
        PlayerInfo playerInfo = this.lastPlayerInfos.get(uuid);
        return playerInfo != null && playerInfo.wonGame();
    }

	public GameType getLastPlayerGameMode(UUID uuid) {
		PlayerInfo playerInfo = this.lastPlayerInfos.get(uuid);
		return playerInfo != null ? playerInfo.gameMode() : null;
	}

}
