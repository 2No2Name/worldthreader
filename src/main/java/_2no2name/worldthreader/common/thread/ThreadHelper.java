package _2no2name.worldthreader.common.thread;

import _2no2name.worldthreader.WorldThreaderMod;
import net.minecraft.server.world.ServerWorld;

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
     * Makes it easy to understand what is happening in crash reports and helps identify dimthread workers.
     */
    public static void attach(Thread thread, String name) {
        thread.setName(WorldThreaderMod.MOD_ID + "_" + name);
    }

    public static void attach(Thread thread, ServerWorld world) {
        attach(thread, world.getRegistryKey().getValue().toString());
    }
}
