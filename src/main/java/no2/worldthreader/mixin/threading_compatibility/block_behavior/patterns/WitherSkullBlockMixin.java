package no2.worldthreader.mixin.threading_compatibility.block_behavior.patterns;

import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import no2.worldthreader.common.mixin_support.interfaces.BeforeThreadingInitialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WitherSkullBlock.class)
public abstract class WitherSkullBlockMixin implements BeforeThreadingInitialization {

    @Shadow
    public static native BlockPattern getOrCreateWitherBase();

    @Shadow
    public static native BlockPattern getOrCreateWitherFull();


    @Override
    public void worldthreader$initBeforeThreading() {
        getOrCreateWitherBase();
        getOrCreateWitherFull();
    }
}
