package no2.worldthreader.mixin.dimension_change.departure;

public class ServerPlayerMixin {
    //Luckily, players tick in the packet phase, which is not parallelized by worldthreader. Thus, handlePortal is also
    // called during a non-multithreaded tick phase. We don't need any special code paths.
    // (besides the threadsafety fix using player object replacement)
}
