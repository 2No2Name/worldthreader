package no2.worldthreader.mixin.threadsafe_scoreboard;

import net.minecraft.server.commands.ScoreboardCommand;
import net.minecraft.world.scores.ScoreAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScoreboardCommand.class)
public class ScoreboardCommandMixin {
    @Redirect(
            method = {
                    "addScore(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Collection;Lnet/minecraft/world/scores/Objective;I)I",
                    "removeScore(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Collection;Lnet/minecraft/world/scores/Objective;I)I"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/ScoreAccess;get()I", ordinal = 0)
    )
    private static int score(ScoreAccess instance) {
        return 0;
    }

    @Redirect(
            method = {
                    "addScore(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Collection;Lnet/minecraft/world/scores/Objective;I)I",
                    "removeScore(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Collection;Lnet/minecraft/world/scores/Objective;I)I"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/ScoreAccess;set(I)V")
    )
    private static void score(ScoreAccess instance, int addAmount) {
        instance.add(addAmount); //This is correct for both addition and subtraction, as the parameter is already 0 + i or 0 - i
    }
}
