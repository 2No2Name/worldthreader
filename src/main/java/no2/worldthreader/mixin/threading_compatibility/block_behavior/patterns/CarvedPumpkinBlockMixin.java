package no2.worldthreader.mixin.threading_compatibility.block_behavior.patterns;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import no2.worldthreader.common.mixin_support.interfaces.BeforeThreadingInitialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinBlockMixin implements BeforeThreadingInitialization {

    @Shadow
    public abstract BlockPattern getOrCreateSnowGolemBase();

    @Shadow
    public abstract BlockPattern getOrCreateIronGolemBase();

    @Shadow
    public abstract BlockPattern getOrCreateSnowGolemFull();

    @Shadow
    public abstract BlockPattern getOrCreateIronGolemFull();


    @Override
    public void worldthreader$initBeforeThreading(MinecraftServer server) {
        this.getOrCreateSnowGolemBase();
        this.getOrCreateSnowGolemFull();

        this.getOrCreateIronGolemBase();
        this.getOrCreateIronGolemFull();
    }
}
