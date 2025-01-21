package no2.worldthreader.mixin.threadsafe_scoreboard.atomic_arithmetic;

import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreAccess;
import no2.worldthreader.common.scoreboard.AtomicArithmeticScore;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/scores/Scoreboard$1")
public abstract class Scoreboard$ScoreAccessMixin implements ScoreAccess, AtomicArithmeticScore {

    @Shadow @Final Score val$score;

    @Override
    public int add(int amount) {
        return this.worldthreader$addToValueAndGet(amount);
    }

    @Override
    public int worldthreader$addToValueAndGet(int amount) {
        if (this.val$score instanceof AtomicArithmeticScore arithmeticScore) {
            return arithmeticScore.worldthreader$addToValueAndGet(amount);
        }
        throw new AssertionError();
    }

    @Override
    public int worldthreader$compareExchangeValue(int value, int expected) {
        if (this.val$score instanceof AtomicArithmeticScore arithmeticScore) {
            return arithmeticScore.worldthreader$compareExchangeValue(value, expected);
        }
        throw new AssertionError();
    }
}
