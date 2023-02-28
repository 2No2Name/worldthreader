package _2no2name.worldthreader.common.scoreboard;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicReference;

public class ThreadsafeScoreboardObjective extends ScoreboardObjective {
    private static final Text DUMMY_TEXT = Text.literal("INVALID");
    private final AtomicReference<ScoreboardObjective> delegate = new AtomicReference<>();

    public ThreadsafeScoreboardObjective(Scoreboard scoreboard, String name, ScoreboardCriterion criterion, Text displayName, ScoreboardCriterion.RenderType renderType) {
        super(null, null, null, DUMMY_TEXT, null);
        this.delegate.set(new ScoreboardObjective(scoreboard, name, criterion, displayName, renderType));
    }

    public ThreadsafeScoreboardObjective(ScoreboardObjective delegate) {
        super(null, null, null, DUMMY_TEXT, null);
        this.delegate.set(delegate);
    }

    public Scoreboard getScoreboard() {
        return this.delegate.get().getScoreboard();
    }

    public String getName() {
        return this.delegate.get().getName();
    }

    public ScoreboardCriterion getCriterion() {
        return this.delegate.get().getCriterion();
    }

    public Text getDisplayName() {
        return this.delegate.get().getDisplayName();
    }

    @Override
    public void setDisplayName(Text name) {
        ScoreboardObjective original, copy;
        do {
            original = this.delegate.get();
            //noinspection unchecked
            copy = ((Copyable<ScoreboardObjective>) original).copy();
            copy.setDisplayName(name);
        } while (!this.delegate.compareAndSet(original, copy));
    }

    public Text toHoverableText() {
        return this.delegate.get().toHoverableText();
    }

    public ScoreboardCriterion.RenderType getRenderType() {
        return this.delegate.get().getRenderType();
    }

    @Override
    public void setRenderType(ScoreboardCriterion.RenderType renderType) {
        ScoreboardObjective original, copy;
        do {
            original = this.delegate.get();
            //noinspection unchecked
            copy = ((Copyable<ScoreboardObjective>) original).copy();
            copy.setRenderType(renderType);
        } while (!this.delegate.compareAndSet(original, copy));

    }
}
