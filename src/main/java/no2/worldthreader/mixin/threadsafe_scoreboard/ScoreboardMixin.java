package no2.worldthreader.mixin.threadsafe_scoreboard;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScores;
import net.minecraft.world.scores.Scoreboard;
import no2.worldthreader.common.scoreboard.ThreadsafeScoreboard;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin {

    @Mutable
    @Shadow @Final private Map<String, PlayerScores> playerScores; //TODO safety of player scores?

    @Shadow @Final private Object2ObjectMap<String, Objective> objectivesByName;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void createThreadsafeCollections(CallbackInfo ci) {
        if (this instanceof ThreadsafeScoreboard) {
            this.playerScores = new ConcurrentHashMap<>(this.playerScores);
        }
    }
    //TODO "ScoreAccess"

    @Inject(
            method = "addObjective(Ljava/lang/String;Lnet/minecraft/world/scores/criteria/ObjectiveCriteria;Lnet/minecraft/network/chat/Component;Lnet/minecraft/world/scores/criteria/ObjectiveCriteria$RenderType;ZLnet/minecraft/network/chat/numbers/NumberFormat;)Lnet/minecraft/world/scores/Objective;",
            at = @At("HEAD")
    )
    private void ensureSafe(CallbackInfoReturnable<Objective> cir) {
        if (this instanceof ThreadsafeScoreboard threadsafeScoreboard) {
            threadsafeScoreboard.worldthreader$ensureExclusiveScoreboardAccess();
        }
    }
    @Inject(
            method = "removeObjective(Lnet/minecraft/world/scores/Objective;)V",
            at = @At("HEAD")
    )
    private void ensureSafe(Objective objective, CallbackInfo ci) {
        if (this instanceof ThreadsafeScoreboard threadsafeScoreboard) {
            threadsafeScoreboard.worldthreader$ensureExclusiveScoreboardAccess();
        }
    }
    /**
     * @author 2No2Name
     * @reason Thread safety
     */
    @Overwrite
    public Collection<Objective> getObjectives() {
        return Object2ObjectMaps.unmodifiable(this.objectivesByName).values();
    }
    /**
     * @author 2No2Name
     * @reason Thread safety
     */
    @Overwrite
    public Collection<String> getObjectiveNames() {
        return Object2ObjectMaps.unmodifiable(this.objectivesByName).keySet();
    }
}
