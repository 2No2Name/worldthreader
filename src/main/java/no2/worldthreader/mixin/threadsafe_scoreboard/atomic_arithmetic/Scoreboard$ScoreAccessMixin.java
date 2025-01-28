package no2.worldthreader.mixin.threadsafe_scoreboard.atomic_arithmetic;

import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import no2.worldthreader.common.scoreboard.AtomicArithmeticScore;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net/minecraft/world/scores/Scoreboard$1")
public abstract class Scoreboard$ScoreAccessMixin implements ScoreAccess, AtomicArithmeticScore {

    @Shadow @Final Score val$score;

    @Shadow
    @Final
    public boolean val$canModify;

    @Shadow
    @Final
    public MutableBoolean val$requiresSync;

    @Shadow
    @Final
    public Objective val$objective;

    @Shadow
    @Final
    public ScoreHolder val$scoreHolder;

    @Shadow protected abstract void sendScoreToPlayers();

    @Override
    public int add(int amount) {
        return this.worldthreader$addToValueAndGet(amount);
    }

    @Override
    public int worldthreader$addToValueAndGet(int amount) {
        if (this.val$score instanceof AtomicArithmeticScore arithmeticScore) {
            boolean requiresSync = this.beforeModify();
            int result = arithmeticScore.worldthreader$addToValueAndGet(amount);
            this.afterModify(requiresSync || amount != 0);
            return result;
        }
        throw new AssertionError();
    }

    @Override
    public int worldthreader$compareExchangeValue(int expectedValue, int newValue) {
        if (this.val$score instanceof AtomicArithmeticScore arithmeticScore) {
            boolean requiresSync = this.beforeModify();
            int witness = arithmeticScore.worldthreader$compareExchangeValue(expectedValue, newValue);
            this.afterModify(requiresSync || expectedValue != newValue && witness == expectedValue);
            return witness;
        }
        throw new AssertionError();
    }

    @Unique
    public boolean beforeModify() {
        if (!this.val$canModify) {
            throw new IllegalStateException("Cannot modify read-only score");
        } else {
            boolean requiresSync = this.val$requiresSync.isTrue();
            if (this.val$objective.displayAutoUpdate()) {
                Component component = this.val$scoreHolder.getDisplayName();
                if (component != null && !component.equals(this.val$score.display())) {
                    this.val$score.display(component);
                    return true;
                }
            }
            return requiresSync;
        }
    }

    @Unique
    public void afterModify(boolean requiresSync) {
        if (requiresSync) {
            this.sendScoreToPlayers();
        }
    }
}
