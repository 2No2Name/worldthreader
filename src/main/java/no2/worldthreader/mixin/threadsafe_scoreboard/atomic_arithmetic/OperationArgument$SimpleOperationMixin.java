package no2.worldthreader.mixin.threadsafe_scoreboard.atomic_arithmetic;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.world.scores.ScoreAccess;
import no2.worldthreader.common.scoreboard.AtomicArithmeticScore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/commands/arguments/OperationArgument$SimpleOperation")
public interface OperationArgument$SimpleOperationMixin {

    @Shadow int apply(int i, int j) throws CommandSyntaxException;

    /**
     * @author 2No2Name
     * @reason Atomic math operations
     */
    @Overwrite
    default void apply(ScoreAccess firstValue, ScoreAccess secondValue) throws CommandSyntaxException {
        int previousFirstValue = firstValue.get();
        if (firstValue instanceof AtomicArithmeticScore arithmeticScore) {
            int newValue;
            int exchangeResult;
            do {
                newValue = this.apply(previousFirstValue, secondValue.get());
                exchangeResult = arithmeticScore.worldthreader$compareExchangeValue(newValue, previousFirstValue);
                previousFirstValue = exchangeResult;
            } while (exchangeResult != newValue);
        } else {
            int result = this.apply(previousFirstValue, secondValue.get());
            firstValue.set(result);
        }
    }
}
