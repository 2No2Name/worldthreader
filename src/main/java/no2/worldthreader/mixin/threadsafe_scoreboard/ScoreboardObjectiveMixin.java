package no2.worldthreader.mixin.threadsafe_scoreboard;

import net.minecraft.network.chat.numbers.NumberFormat;
import no2.worldthreader.common.scoreboard.Copyable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Objective.class)
public abstract class ScoreboardObjectiveMixin implements Cloneable, Copyable<Objective> {
    @Shadow
    public abstract Scoreboard getScoreboard();

    @Shadow
    public abstract String getName();

    @Shadow
    public abstract Component getDisplayName();

    @Shadow
    public abstract ObjectiveCriteria.RenderType getRenderType();

    @Shadow @Final private Scoreboard scoreboard;

    @Shadow @Final private String name;

    @Shadow @Final private ObjectiveCriteria criteria;

    @Shadow private Component displayName;

    @Shadow private Component formattedDisplayName;

    @Shadow private ObjectiveCriteria.RenderType renderType;

    @Shadow private boolean displayAutoUpdate;

    @Shadow private @Nullable NumberFormat numberFormat;

    @Override
    public Objective worldthreader$copy() {
        try {
            return (Objective) this.clone();
        } catch (CloneNotSupportedException e) {
            return new Objective(this.scoreboard, this.name, this.criteria, this.displayName, this.renderType, this.displayAutoUpdate, this.numberFormat);
        }
    }
}
