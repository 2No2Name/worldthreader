package _2no2name.worldthreader.mixin.thread_ownership;

import _2no2name.worldthreader.common.thread.IThreadOwnedObject;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
public abstract class WorldMixin implements IThreadOwnedObject {

    @Mutable
    @Shadow
    @Final
    private Thread thread;

    @Override
    public Thread getOwningThread() {
        return this.thread;
    }

    @Override
    public void setOwningThread(Thread thread) {
        this.thread = thread;
    }

}
