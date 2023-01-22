package _2no2name.worldthreader.common.mixin_support.interfaces;

import java.util.concurrent.atomic.AtomicInteger;

public interface ServerWorldWithWirelessRedstoneFix {

    int getNextEntityId(AtomicInteger entityIdCounter);
}
