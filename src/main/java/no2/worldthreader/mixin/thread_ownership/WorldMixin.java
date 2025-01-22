package no2.worldthreader.mixin.thread_ownership;

import no2.worldthreader.common.thread.ThreadOwnedObject;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;

@Mixin(Level.class)
public abstract class WorldMixin implements ThreadOwnedObject {

    @Mutable
    @Shadow
    @Final
    private Thread thread;

    @Override
    public Thread worldthreader$getOwningThread() {
        return this.thread;
    }

    @Override
    public void worldthreader$setOwningThread(Thread thread) {
        this.thread = thread;
    }
}
