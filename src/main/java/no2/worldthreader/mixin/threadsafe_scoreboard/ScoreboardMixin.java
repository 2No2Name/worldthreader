package no2.worldthreader.mixin.threadsafe_scoreboard;

import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin {

//    @Mutable
//    @Shadow
//    @Final
//    private Map<ObjectiveCriteria, List<Objective>> objectivesByCriterion;
//
//    @Mutable
//    @Shadow
//    @Final
//    private Map<String, Map<Objective, ScoreboardScore>> playerObjectives;
//
//    @Mutable
//    @Shadow
//    @Final
//    private Map<String, PlayerTeam> teams;
//
//    @Mutable
//    @Shadow
//    @Final
//    private Map<String, PlayerTeam> teamsByPlayer;
//
//    @Inject(
//            method = "<init>",
//            at = @At("RETURN")
//    )
//    private void createThreadsafeCollections(CallbackInfo ci) {
//        if (this instanceof ThreadsafeScoreboard) {
//            this.objectivesByCriterion = new ConcurrentHashMap<>(this.objectivesByCriterion);
//            this.playerObjectives = new ConcurrentHashMap<>(this.playerObjectives);
//            this.teams = new ConcurrentHashMap<>(this.teams);
//            this.teamsByPlayer = new ConcurrentHashMap<>(this.teamsByPlayer);
//        }
//    }
//
//    @Redirect(
//            method = "addObjective(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreboardCriterion;Lnet/minecraft/text/Text;Lnet/minecraft/scoreboard/ScoreboardCriterion$RenderType;)Lnet/minecraft/scoreboard/ScoreboardObjective;",
//            at = @At(value = "NEW", target = "net/minecraft/scoreboard/ScoreboardObjective")
//    )
//    private Objective createThreadsafeObjective(Scoreboard scoreboard, String name, ObjectiveCriteria criterion, Component displayName, ObjectiveCriteria.RenderType renderType) {
//        return new ThreadsafeScoreboardObjective(scoreboard, name, criterion, displayName, renderType);
//    }
//
//    @Redirect(
//            method = "addObjective(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreboardCriterion;Lnet/minecraft/text/Text;Lnet/minecraft/scoreboard/ScoreboardCriterion$RenderType;)Lnet/minecraft/scoreboard/ScoreboardObjective;",
//            at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;")
//    )
//    private <K, V> Object useCopyOnWriteArrayList(Map<K, List<V>> map, K key, Function<? super K, ? extends List<V>> mappingFunction) {
//        return map.computeIfAbsent(key, (K a) -> new CopyOnWriteArrayList<>());
//    }
//
//    @Redirect(
//            method = "getPlayerScore(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreboardObjective;)Lnet/minecraft/scoreboard/ScoreboardPlayerScore;",
//
//            at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;", ordinal = 0)
//    )
//    private <K, X, W> Object useConcurrentHashMap(Map<K, Map<X, W>> map, K key, Function<? super K, ? extends Map<X, W>> mappingFunction) {
//        return map.computeIfAbsent(key, (K a) -> new ConcurrentHashMap<X, W>());
//    }
}
