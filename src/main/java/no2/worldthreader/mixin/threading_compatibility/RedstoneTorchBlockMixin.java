package no2.worldthreader.mixin.threading_compatibility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import no2.worldthreader.common.mixin_support.interfaces.BeforeThreadingInitialization;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(RedstoneTorchBlock.class)
public class RedstoneTorchBlockMixin implements BeforeThreadingInitialization {

    @Mutable
    @Shadow
    @Final
    private static Map<BlockGetter, List<RedstoneTorchBlock.Toggle>> RECENT_TOGGLES;

    @Override
    public void worldthreader$initBeforeThreading(MinecraftServer server) {
        WeakHashMap<BlockGetter, List<RedstoneTorchBlock.Toggle>> newMap = new WeakHashMap<>();
        for (Level level : server.getAllLevels()) {
            newMap.put(level, RECENT_TOGGLES.get(level));
        }
        RECENT_TOGGLES = newMap;
    }
}
