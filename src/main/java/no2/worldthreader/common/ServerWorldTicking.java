package no2.worldthreader.common;

import net.minecraft.util.profiling.Profiler;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import no2.worldthreader.common.thread.ThreadOwnedObject;
import no2.worldthreader.common.thread.ThreadHelper;
import no2.worldthreader.common.thread.WorldThreadingManager;
import net.minecraft.CrashReport;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.DerivedLevelData;
import java.util.function.BooleanSupplier;

public class ServerWorldTicking {

    public static boolean isMainWorld(ServerLevel world) {
        return !(world.getLevelData() instanceof DerivedLevelData);
    }


    public static void runWorldThread(MinecraftServer server, WorldThreadingManager worldThreadingManager, ServerLevel serverWorld, ThreadOwnedObject[] threadOwnedObjects) {
        Thread currentThread = Thread.currentThread();
        boolean continueMultithreading = true;
        while (continueMultithreading) {
            //Start of tick barrier
            if (worldThreadingManager.tickBarrier() < 0) {
                continueMultithreading = false;
            } else {
                Thread mainThread = ((ThreadOwnedObject) serverWorld).worldthreader$getOwningThread();
                ThreadHelper.swapOnMultithreadTickStart(mainThread, currentThread, threadOwnedObjects);
                tickThreaded(server, worldThreadingManager, serverWorld);
                ThreadHelper.swapOnMultithreadTickEnd(mainThread, currentThread, threadOwnedObjects);
                //End of tick barrier
                if (worldThreadingManager.tickBarrier() < 0) {
                    continueMultithreading = false;
                }
            }
        }
    }

    public static void tickThreaded(MinecraftServer server, WorldThreadingManager worldThreadingManager, ServerLevel serverLevel) {
        //TODO Issues mostly with Command Blocks: Setting Gamerules, Scoreboards, Time / Level Properties (Difficulty etc), Level Info is not threadsafe.

        final BooleanSupplier shouldKeepTicking = worldThreadingManager::shouldKeepTickingThreaded;

        try {
            // [VanillaCopy] MinecraftServer#tickChildren
            ProfilerFiller profilerFiller = Profiler.get();
            profilerFiller.push(() -> serverLevel + " " + serverLevel.dimension().location());
            if (server.getTickCount() % 20 == 0) {
                profilerFiller.push("timeSync");
                server.synchronizeTime(serverLevel);
                profilerFiller.pop();
            }
            profilerFiller.push("tick");
            try {
                serverLevel.tick(shouldKeepTicking);
            } catch (Throwable throwable) {
                delegateCrash(throwable, "Exception ticking world", serverLevel, worldThreadingManager);
            }

            try {
                worldThreadingManager.withinTickBarrier();
                finishTeleportsToWorld(serverLevel);
            } catch (Throwable throwable) {
                delegateCrash(throwable, "Exception receiving entities from other worlds", serverLevel, worldThreadingManager);
            }

            try {
                worldThreadingManager.withinTickBarrier();
                recoverFailedTeleports(serverLevel);
            } catch (Throwable throwable) {
                delegateCrash(throwable, "Exception restoring entities that could not be teleported to another world", serverLevel, worldThreadingManager);
            }
            profilerFiller.pop();
            profilerFiller.pop();
        } catch (Throwable throwable) {
            delegateCrash(throwable, "Exception in server world thread", serverLevel, worldThreadingManager);
        }
    }

    private static void delegateCrash(Throwable throwable, String title, ServerLevel serverWorld, WorldThreadingManager worldThreadingManager) {
        CrashReport crashReport = CrashReport.forThrowable(throwable, title);
        serverWorld.fillReportDetails(crashReport);
        worldThreadingManager.handleCrash(crashReport);

    }

    public static void finishTeleportsToWorld(ServerLevel world) {
        ((ServerWorldExtended) world).worldthreader$finishReceivingTeleportedEntities();
    }

    public static void recoverFailedTeleports(ServerLevel world) {
        ((ServerWorldExtended) world).worldthreader$recoverFailedTeleports();
    }
}
