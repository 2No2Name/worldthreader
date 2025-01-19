package no2.worldthreader.common.thread;

public interface IThreadOwnedObject {

    Thread getOwningThread();

    void setOwningThread(Thread thread);

}
