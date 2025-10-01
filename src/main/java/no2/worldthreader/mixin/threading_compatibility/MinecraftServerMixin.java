package no2.worldthreader.mixin.threading_compatibility;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerExtended {

    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Shadow
    public abstract Iterable<ServerLevel> getAllLevels();

    @Redirect(
            method = "getGameRules()Lnet/minecraft/world/level/GameRules;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;")
    )
    private ServerLevel getOverworldDirect(MinecraftServer instance) {
        return this.worldthreader$getLevelUnsynchronized(Level.OVERWORLD);
    }

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
}
