package no2.worldthreader.common.mixin_support.interfaces;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface TransparentServerPlayerSwapper {
    ServerPlayer worldthreader$swapRemovedPlayerWithNewCopy(ServerPlayer previous, ServerLevel newLevel);
}
