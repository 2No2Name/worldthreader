package _2no2name.worldthreader.common.mixin_support.interfaces;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

public interface EntityExtended {
    void arriveInWorld(NbtCompound entityNBT, ServerWorld destination);

    default void onArrivedInWorld() {
    }
}
