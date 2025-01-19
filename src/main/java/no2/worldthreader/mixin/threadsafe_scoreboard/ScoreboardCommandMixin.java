package no2.worldthreader.mixin.threadsafe_scoreboard;

import net.minecraft.server.commands.ScoreboardCommand;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ScoreboardCommand.class)
public class ScoreboardCommandMixin {
//    @Redirect(
//            method = {
//                    "executeAdd(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/scoreboard/ScoreboardObjective;I)I",
//                    "executeRemove(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/scoreboard/ScoreboardObjective;I)I"
//            },
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardPlayerScore;getScore()I")
//    )
//    private static int score(ScoreboardScore scoreboardPlayerScore) {
//        return 0;
//    }
//
//    @Redirect(
//            method = {
//                    "executeAdd(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/scoreboard/ScoreboardObjective;I)I",
//                    "executeRemove(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/scoreboard/ScoreboardObjective;I)I"
//            },
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardPlayerScore;setScore(I)V")
//    )
//    private static void score(ScoreboardScore scoreboardPlayerScore, int score) {
//        ((ScoreboardScoreAccess) scoreboardPlayerScore).forceAddScore(score);
//    }
}
