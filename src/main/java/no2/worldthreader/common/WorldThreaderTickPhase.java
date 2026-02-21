package no2.worldthreader.common;

public enum WorldThreaderTickPhase {
    NONE,
    WORLD_TICK,
    RECEIVE_TELEPORTS,
    TICK_AFTER_TELEPORT,
    RECOVER_FAILED_TELEPORTS,
    EXTRA_TICK_TELEPORTS
}
