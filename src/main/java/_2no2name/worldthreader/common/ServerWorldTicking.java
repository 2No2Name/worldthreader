package _2no2name.worldthreader.common;

import _2no2name.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import _2no2name.worldthreader.common.thread.IThreadOwnedObject;
import _2no2name.worldthreader.common.thread.ThreadHelper;
import _2no2name.worldthreader.common.thread.WorldThreadingManager;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameRules;
import net.minecraft.world.level.UnmodifiableLevelProperties;

import java.util.function.BooleanSupplier;

public class ServerWorldTicking {

    public static boolean isMainWorld(ServerWorld world) {
        return !(world.getLevelProperties() instanceof UnmodifiableLevelProperties);
    }


    public static void runWorldThread(MinecraftServer server, WorldThreadingManager worldThreadingManager, ServerWorld serverWorld) {
        Thread currentThread = Thread.currentThread();
        boolean continueMultithreading = true;
        while (continueMultithreading) {
            //Start of tick barrier
            if (worldThreadingManager.tickBarrier() < 0) {
                continueMultithreading = false;
            } else {
                Thread mainThread = ((IThreadOwnedObject) serverWorld).getOwningThread();
                ThreadHelper.swapOnMultithreadTickStart(mainThread, currentThread, ((IThreadOwnedObject) serverWorld), (IThreadOwnedObject) serverWorld.getChunkManager());
                tickThreaded(server, worldThreadingManager, serverWorld);
                ThreadHelper.swapOnMultithreadTickEnd(mainThread, currentThread, ((IThreadOwnedObject) serverWorld), (IThreadOwnedObject) serverWorld.getChunkManager());
                //End of tick barrier
                if (worldThreadingManager.tickBarrier() < 0) {
                    continueMultithreading = false;
                }
            }
        }
    }

    public static void tickThreaded(MinecraftServer server, WorldThreadingManager worldThreadingManager, ServerWorld serverWorld) {
        //TODO Issues mostly with Command Blocks: Setting Gamerules, Scoreboards, Time / Level Properties (Difficulty etc), Level Info is not threadsafe.

        final BooleanSupplier shouldKeepTicking = worldThreadingManager::shouldKeepTickingThreaded;

        try {
            Profiler worldProfiler = serverWorld.getProfiler();
            worldProfiler.push(() -> serverWorld + " " + serverWorld.getRegistryKey().getValue());
            if (server.getTicks() % 20 == 0) {
                worldProfiler.push("timeSync");
                server.getPlayerManager().sendToDimension(new WorldTimeUpdateS2CPacket(serverWorld.getTime(), serverWorld.getTimeOfDay(), serverWorld.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)), serverWorld.getRegistryKey());
                worldProfiler.pop();
            }
            worldProfiler.push("tick");
            try {
                serverWorld.tick(shouldKeepTicking);
            } catch (Throwable throwable) {
                delegateCrash(throwable, "Exception ticking world", serverWorld, worldThreadingManager);
            }

            try {
                worldThreadingManager.withinTickBarrier();
                finishTeleportsToWorld(serverWorld);
            } catch (Throwable throwable) {
                delegateCrash(throwable, "Exception receiving entities from other worlds", serverWorld, worldThreadingManager);
            }

            try {
                worldThreadingManager.withinTickBarrier();
                recoverFailedTeleports(serverWorld);
            } catch (Throwable throwable) {
                delegateCrash(throwable, "Exception restoring entities that could not be teleported to another world", serverWorld, worldThreadingManager);
            }
            worldProfiler.pop();
            worldProfiler.pop();
        } catch (Throwable throwable) {
            delegateCrash(throwable, "Exception in server world thread", serverWorld, worldThreadingManager);
        }
    }

    private static void delegateCrash(Throwable throwable, String title, ServerWorld serverWorld, WorldThreadingManager worldThreadingManager) {
        CrashReport crashReport = CrashReport.create(throwable, title);
        serverWorld.addDetailsToCrashReport(crashReport);
        worldThreadingManager.handleCrash(crashReport);

    }

    public static void finishTeleportsToWorld(ServerWorld world) {
        ((ServerWorldExtended) world).finishReceivingTeleportedEntities();
    }

    public static void recoverFailedTeleports(ServerWorld world) {
        ((ServerWorldExtended) world).recoverFailedTeleports();
    }
}
