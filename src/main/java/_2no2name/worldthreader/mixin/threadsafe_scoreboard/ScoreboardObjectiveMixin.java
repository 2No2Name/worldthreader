package _2no2name.worldthreader.mixin.threadsafe_scoreboard;

import _2no2name.worldthreader.common.scoreboard.Copyable;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ScoreboardObjective.class)
public abstract class ScoreboardObjectiveMixin implements Cloneable, Copyable<ScoreboardObjective> {
    @Shadow
    public abstract Scoreboard getScoreboard();

    @Shadow
    public abstract String getName();

    @Shadow
    public abstract ScoreboardCriterion getCriterion();

    @Shadow
    public abstract Text getDisplayName();

    @Shadow
    public abstract ScoreboardCriterion.RenderType getRenderType();

    @Override
    public ScoreboardObjective copy() {
        try {
            return (ScoreboardObjective) this.clone();
        } catch (CloneNotSupportedException e) {
            return new ScoreboardObjective(this.getScoreboard(), this.getName(), this.getCriterion(), this.getDisplayName(), this.getRenderType());
        }
    }
}
