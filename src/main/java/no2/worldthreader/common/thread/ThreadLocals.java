package no2.worldthreader.common.thread;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import no2.worldthreader.common.tuples.Pair;

public class ThreadLocals {

    public static final ThreadLocal<Pair<Direction.Axis, Vec3>> NETHER_PORTAL_POSITION_INFO = new ThreadLocal<>();

}
