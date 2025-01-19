package no2.worldthreader.common.thread;

import no2.worldthreader.WorldThreaderMod;
import net.minecraft.server.level.ServerLevel;

public class ThreadHelper {

    public static void swapOnMultithreadTickStart(Thread mainThread, Thread worldThread, IThreadOwnedObject... threadExclusiveObjects) {
        for (IThreadOwnedObject object : threadExclusiveObjects) {
            if (mainThread == object.getOwningThread()) {
                object.setOwningThread(worldThread);
            } else {
                throw new IllegalStateException("Failed to swap thread exclusive access!");
            }
        }
    }

    public static void swapOnMultithreadTickEnd(Thread mainThread, Thread worldThread, IThreadOwnedObject... threadExclusiveObjects) {
        for (IThreadOwnedObject object : threadExclusiveObjects) {
            if (worldThread == object.getOwningThread()) {
                object.setOwningThread(mainThread);
            } else {
                throw new IllegalStateException("Failed to swap thread exclusive access!");
            }
        }
    }


    /**
     * Makes it easy to understand what is happening in crash reports and helps identify worker threads.
     */
    public static void attach(Thread thread, String name) {
        thread.setName(WorldThreaderMod.MOD_ID + "_" + name);
    }

    public static void attach(Thread thread, ServerLevel world) {
        attach(thread, world.dimension().location().toString());
    }
}
