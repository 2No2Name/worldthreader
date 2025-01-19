package no2.worldthreader.common.scoreboard;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.Nullable;

public class ThreadsafeScoreboardObjective extends Objective {
    public ThreadsafeScoreboardObjective(Scoreboard scoreboard, String string, ObjectiveCriteria objectiveCriteria, Component component, ObjectiveCriteria.RenderType renderType, boolean bl, @Nullable NumberFormat numberFormat) {
        super(scoreboard, string, objectiveCriteria, component, renderType, bl, numberFormat);
    }
//    private static final Component DUMMY_TEXT = Component.literal("INVALID");
//    private final AtomicReference<Objective> delegate = new AtomicReference<>();
//
//    public ThreadsafeScoreboardObjective(Scoreboard scoreboard, String name, ObjectiveCriteria criterion, Component displayName, ObjectiveCriteria.RenderType renderType) {
//        super(null, null, null, DUMMY_TEXT, null);
//        this.delegate.set(new Objective(scoreboard, name, criterion, displayName, renderType));
//    }
//
//    public ThreadsafeScoreboardObjective(Objective delegate) {
//        super(null, null, null, DUMMY_TEXT, null);
//        this.delegate.set(delegate);
//    }
//
//    public Scoreboard getScoreboard() {
//        return this.delegate.get().getScoreboard();
//    }
//
//    public String getName() {
//        return this.delegate.get().getName();
//    }
//
//    public ObjectiveCriteria getCriteria() {
//        return this.delegate.get().getCriteria();
//    }
//
//    public Component getDisplayName() {
//        return this.delegate.get().getDisplayName();
//    }
//
//    @Override
//    public void setDisplayName(Component name) {
//        Objective original, copy;
//        do {
//            original = this.delegate.get();
//            //noinspection unchecked
//            copy = ((Copyable<Objective>) original).worldthreader$copy();
//            copy.setDisplayName(name);
//        } while (!this.delegate.compareAndSet(original, copy));
//    }
//
//    public Component getFormattedDisplayName() {
//        return this.delegate.get().getFormattedDisplayName();
//    }
//
//    public ObjectiveCriteria.RenderType getRenderType() {
//        return this.delegate.get().getRenderType();
//    }
//
//    @Override
//    public void setRenderType(ObjectiveCriteria.RenderType renderType) {
//        Objective original, copy;
//        do {
//            original = this.delegate.get();
//            //noinspection unchecked
//            copy = ((Copyable<Objective>) original).worldthreader$copy();
//            copy.setRenderType(renderType);
//        } while (!this.delegate.compareAndSet(original, copy));
//
//    }
}
