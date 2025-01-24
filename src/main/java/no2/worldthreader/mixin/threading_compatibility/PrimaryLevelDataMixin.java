package no2.worldthreader.mixin.threading_compatibility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.timers.TimerQueue;
import no2.worldthreader.common.mixin_support.interfaces.PrimaryLevelDataExtended;
import no2.worldthreader.common.thread.ThreadLocals;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataMixin implements ServerLevelData, WorldData, PrimaryLevelDataExtended {

    @Shadow
    @Final
    private TimerQueue<MinecraftServer> scheduledEvents;

    //For all other fields there was a different solution. The thread local minecraft server access for
    // world threads was added last, but it might actually be the better way to ensure mod compatibility
    // TODO: Consider changing this for other fields to reduce amount of code and increase mod compatibility.
    //  But be careful to avoid non-necessary usages of exclusive world access.
    @Inject(
            method = "getScheduledEvents", at = @At(value = "HEAD")
    )
    private void ensureSafety(CallbackInfoReturnable<TimerQueue<MinecraftServer>> cir) {
        MinecraftServer minecraftServer = ThreadLocals.WORLD_THREAD_MINECRAFT_SERVER_ACCESS.get();
        if (minecraftServer != null) {
            minecraftServer.getAllLevels();
        }
    }

    @Override
    public TimerQueue<MinecraftServer> worldthreader$getScheduledEventsUnsafe() {
        return this.scheduledEvents;
    }
}
