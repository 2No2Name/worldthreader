package no2.worldthreader.mixin.threadsafe_scoreboard;

import net.minecraft.server.commands.ScoreboardCommand;
import no2.worldthreader.common.scoreboard.ScoreboardScoreAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScoreboardCommand.class)
public class ScoreboardCommandMixin {
    @Redirect(
            method = {
                    "addObjective(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;Lnet/minecraft/world/scores/criteria/ObjectiveCriteria;Lnet/minecraft/network/chat/Component;)I",
                    "executeRemove(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/scoreboard/ScoreboardObjective;I)I"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardPlayerScore;getScore()I")
    )
    private static int score(ScoreboardScore scoreboardPlayerScore) {
        return 0;
    }

    @Redirect(
            method = {
                    "addObjective(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;Lnet/minecraft/world/scores/criteria/ObjectiveCriteria;Lnet/minecraft/network/chat/Component;)I",
                    "executeRemove(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/scoreboard/ScoreboardObjective;I)I"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardPlayerScore;setScore(I)V")
    )
    private static void score(ScoreboardScore scoreboardPlayerScore, int score) {
        ((ScoreboardScoreAccess) scoreboardPlayerScore).forceAddScore(score);
    }
}
