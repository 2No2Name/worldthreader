package _2no2name.worldthreader.mixin.threadsafe_scoreboard;

import _2no2name.worldthreader.common.scoreboard.ThreadsafeScoreboard;
import _2no2name.worldthreader.common.scoreboard.ThreadsafeScoreboardObjective;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin {

    @Mutable
    @Shadow
    @Final
    private Map<ScoreboardCriterion, List<ScoreboardObjective>> objectivesByCriterion;

    @Mutable
    @Shadow
    @Final
    private Map<String, Map<ScoreboardObjective, ScoreboardPlayerScore>> playerObjectives;

    @Mutable
    @Shadow
    @Final
    private Map<ScoreboardDisplaySlot, ScoreboardObjective> objectiveSlots;

    @Mutable
    @Shadow
    @Final
    private Map<String, Team> teams;

    @Mutable
    @Shadow
    @Final
    private Map<String, Team> teamsByPlayer;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void createThreadsafeCollections(CallbackInfo ci) {
        if (this instanceof ThreadsafeScoreboard) {
            this.objectivesByCriterion = new ConcurrentHashMap<>(this.objectivesByCriterion);
            this.playerObjectives = new ConcurrentHashMap<>(this.playerObjectives);
            this.objectiveSlots = new ConcurrentHashMap<>(this.objectiveSlots);
            this.teams = new ConcurrentHashMap<>(this.teams);
            this.teamsByPlayer = new ConcurrentHashMap<>(this.teamsByPlayer);
        }
    }

    @Redirect(
            method = "addObjective(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreboardCriterion;Lnet/minecraft/text/Text;Lnet/minecraft/scoreboard/ScoreboardCriterion$RenderType;)Lnet/minecraft/scoreboard/ScoreboardObjective;",
            at = @At(value = "NEW", target = "(Lnet/minecraft/scoreboard/Scoreboard;Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreboardCriterion;Lnet/minecraft/text/Text;Lnet/minecraft/scoreboard/ScoreboardCriterion$RenderType;)Lnet/minecraft/scoreboard/ScoreboardObjective;")
    )
    private ScoreboardObjective createThreadsafeObjective(Scoreboard scoreboard, String name, ScoreboardCriterion criterion, Text displayName, ScoreboardCriterion.RenderType renderType) {
        return new ThreadsafeScoreboardObjective(scoreboard, name, criterion, displayName, renderType);
    }

    @Redirect(
            method = "addObjective(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreboardCriterion;Lnet/minecraft/text/Text;Lnet/minecraft/scoreboard/ScoreboardCriterion$RenderType;)Lnet/minecraft/scoreboard/ScoreboardObjective;",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;")
    )
    private <K, V> Object useCopyOnWriteArrayList(Map<K, List<V>> map, K key, Function<? super K, ? extends List<V>> mappingFunction) {
        return map.computeIfAbsent(key, (K a) -> new CopyOnWriteArrayList<>());
    }

    @Redirect(
            method = "getPlayerScore(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreboardObjective;)Lnet/minecraft/scoreboard/ScoreboardPlayerScore;",

            at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;", ordinal = 0)
    )
    private <K, X, W> Object useConcurrentHashMap(Map<K, Map<X, W>> map, K key, Function<? super K, ? extends Map<X, W>> mappingFunction) {
        return map.computeIfAbsent(key, (K a) -> new ConcurrentHashMap<X, W>());
    }
}
