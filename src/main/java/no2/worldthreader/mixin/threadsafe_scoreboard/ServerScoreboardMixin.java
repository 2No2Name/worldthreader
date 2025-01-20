package no2.worldthreader.mixin.threadsafe_scoreboard;


import net.minecraft.server.MinecraftServer;
import no2.worldthreader.common.scoreboard.ThreadsafeScoreboard;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Scoreboard;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin extends Scoreboard implements ThreadsafeScoreboard {


    @SuppressWarnings("ShadowModifiers")
    @Shadow @Final public MinecraftServer server;

    @Override
    public void worldthreader$ensureExclusiveScoreboardAccess() {
        WorldThreadingManager.ensureExclusiveScoreboardAccess(this.server);
    }

    @Override
    public void worldthreader$crashIfNoExclusiveScoreboardAccess() {
        WorldThreadingManager.crashIfNoExclusiveScoreboardAccess(this.server);
    }

    @Inject(
            method = {
                    "setDisplayObjective(Lnet/minecraft/world/scores/DisplaySlot;Lnet/minecraft/world/scores/Objective;)V",
                    "stopTrackingObjective(Lnet/minecraft/world/scores/Objective;)V",
                    "startTrackingObjective(Lnet/minecraft/world/scores/Objective;)V",
                    "addDirtyListener(Ljava/lang/Runnable;)V"
            }, at = @At("HEAD")
    )
    private void ensureSafe(CallbackInfo ci) {
        this.worldthreader$ensureExclusiveScoreboardAccess();
    }
}