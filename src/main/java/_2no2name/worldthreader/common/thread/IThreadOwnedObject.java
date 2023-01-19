package _2no2name.worldthreader.common.thread;

public interface IThreadOwnedObject {

    Thread getOwningThread();

    void setOwningThread(Thread thread);

}
