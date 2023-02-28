package _2no2name.worldthreader.mixin.threadsafe_scoreboard;

import _2no2name.worldthreader.common.scoreboard.ScoreboardScoreAccess;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.command.ScoreboardCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScoreboardCommand.class)
public class ScoreboardCommandMixin {
    @Redirect(
            method = {
                    "executeAdd(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/scoreboard/ScoreboardObjective;I)I",
                    "executeRemove(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/scoreboard/ScoreboardObjective;I)I"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardPlayerScore;getScore()I")
    )
    private static int score(ScoreboardPlayerScore scoreboardPlayerScore) {
        return 0;
    }

    @Redirect(
            method = {
                    "executeAdd(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/scoreboard/ScoreboardObjective;I)I",
                    "executeRemove(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/scoreboard/ScoreboardObjective;I)I"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardPlayerScore;setScore(I)V")
    )
    private static void score(ScoreboardPlayerScore scoreboardPlayerScore, int score) {
        ((ScoreboardScoreAccess) scoreboardPlayerScore).forceAddScore(score);
    }
}
