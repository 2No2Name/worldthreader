package no2.worldthreader.mixin.threading_compatibility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.timers.TimerQueue;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.thread.ThreadLocals;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerExtended {

    @Shadow
    public abstract Iterable<ServerLevel> getAllLevels();


    @Shadow
    @Final
    private TimerQueue<MinecraftServer> scheduledEvents;

    @Inject(
            method = "getCustomBossEvents", at = @At("HEAD")
    )
    private void ensureSafe(CallbackInfoReturnable<CustomBossEvents> cir) {
        this.getAllLevels();
    }

    @Inject(
            method = "getPackRepository", at = @At("HEAD")
    )
    private void ensureSafe1(CallbackInfoReturnable<PackRepository> cir) {
        this.getAllLevels();
    }

    @Inject(
            method = {"startTimeProfiler", "startRecordingMetrics", "stopRecordingMetrics", "finishRecordingMetrics", "cancelRecordingMetrics", "setDefaultGameType"}, at = @At("HEAD")
    )
    private void ensureSafe1(CallbackInfo ci) {
        this.getAllLevels();
    }

    @Inject(
            method = "stopTimeProfiler", at = @At("HEAD")
    )
    private void ensureSafe2(CallbackInfoReturnable<ProfileResults> cir) {
        this.getAllLevels();
    }

    @Inject(
            method = "setDifficulty", at = @At("HEAD")
    )
    private void ensureSafe2(Difficulty difficulty, boolean bl, CallbackInfo ci) {
        this.getAllLevels();
    }

    @Inject(
            method = "setRespawnData", at = @At("HEAD")
    )
    private void ensureSafe2(LevelData.RespawnData respawnData, CallbackInfo ci) {
        this.getAllLevels();
    }

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
