package no2.worldthreader.mixin.threadsafe_scoreboard.teams;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import no2.worldthreader.common.scoreboard.MutablePlayerTeam;
import no2.worldthreader.common.scoreboard.ThreadsafeScoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {

    @Shadow @Final private Object2ObjectMap<String, PlayerTeam> teamsByName;

    /**
     * @author 2No2Name
     * @reason Thread safety
     */
    @Overwrite
    public Collection<String> getTeamNames() {
        return Object2ObjectMaps.unmodifiable(this.teamsByName).keySet();
    }
    /**
     * @author 2No2Name
     * @reason Thread safety
     */
    @Overwrite
    public Collection<PlayerTeam> getPlayerTeams() {
        return Object2ObjectMaps.unmodifiable(this.teamsByName).values();
    }

    @Inject(
            method = "removePlayerTeam(Lnet/minecraft/world/scores/PlayerTeam;)V",
            at = @At("HEAD")
    )
    private void ensureSafe(CallbackInfo ci) {
        if (this instanceof ThreadsafeScoreboard threadsafeScoreboard) {
            threadsafeScoreboard.worldthreader$ensureExclusiveScoreboardAccess();
        }
    }

    @Redirect(
            method = {
                    "addPlayerToTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)Z",
                    "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/scores/PlayerTeam;getPlayers()Ljava/util/Collection;"
            )
    )
    private Collection<String> getUnsafePlayerSet(PlayerTeam playerTeam) {
        return ((MutablePlayerTeam) playerTeam).worldthreader$getMutablePlayerSet();
    }
    @Inject(
            method = "addPlayerToTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)Z",
            at = @At(
                    value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Ljava/util/Collection;add(Ljava/lang/Object;)Z"
            )
    )
    private void notifyAboutModification(String string, PlayerTeam playerTeam, CallbackInfoReturnable<Boolean> cir) {
        ((MutablePlayerTeam) playerTeam).worldthreader$onMutablePlayerSetModifiedExternally();
    }
    @Inject(
            method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V",
            at = @At(
                    value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Ljava/util/Collection;remove(Ljava/lang/Object;)Z"
            )
    )
    private void notifyAboutModification(String string, PlayerTeam playerTeam, CallbackInfo ci) {
        ((MutablePlayerTeam) playerTeam).worldthreader$onMutablePlayerSetModifiedExternally();
    }
}
