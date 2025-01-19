package no2.worldthreader.mixin.thread_ownership;

import no2.worldthreader.common.thread.IThreadOwnedObject;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Level.class)
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
