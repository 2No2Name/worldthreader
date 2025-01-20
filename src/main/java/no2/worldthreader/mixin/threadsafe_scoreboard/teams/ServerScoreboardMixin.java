package no2.worldthreader.mixin.threadsafe_scoreboard.teams;

import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Scoreboard;
import no2.worldthreader.common.scoreboard.ThreadsafeScoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerScoreboard.class)
public abstract class ServerScoreboardMixin extends Scoreboard implements ThreadsafeScoreboard {

    @Inject(
            method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V",
            at = @At("HEAD")
    )
    private void ensureSafe(CallbackInfo ci) {
        this.worldthreader$ensureExclusiveScoreboardAccess();
    }

    @Inject(
            method = "addPlayerToTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)Z",
            at = @At("HEAD")
    )
    private void ensureSafe1(CallbackInfoReturnable<Boolean> cir) {
        this.worldthreader$ensureExclusiveScoreboardAccess();
    }
}
