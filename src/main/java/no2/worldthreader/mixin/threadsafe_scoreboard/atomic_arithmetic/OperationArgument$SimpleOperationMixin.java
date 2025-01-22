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
        int previousValue;
        if (firstValue instanceof AtomicArithmeticScore arithmeticScore) {
            int newValue;
            int witness = firstValue.get();
            do {
                previousValue = witness;
                newValue = this.apply(previousValue, secondValue.get());
                witness = arithmeticScore.worldthreader$compareExchangeValue(previousValue, newValue);
            } while (witness != previousValue);
        } else {
            int result = this.apply(firstValue.get(), secondValue.get());
            firstValue.set(result);
        }
    }
}
