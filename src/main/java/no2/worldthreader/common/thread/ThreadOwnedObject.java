package no2.worldthreader.common.thread;

public interface ThreadOwnedObject {

    Thread worldthreader$getOwningThread();

    void worldthreader$setOwningThread(Thread thread);

}
