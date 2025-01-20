package no2.worldthreader.mixin.threadsafe_scoreboard;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.ScoreboardSaveData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ScoreboardSaveData.class)
public abstract class ScoreboardSaveDataMixin extends SavedData {

    @Unique
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    @Override
    public void setDirty(boolean bl) {
        this.dirty.set(bl);
    }

    @Override
    public boolean isDirty() {
        return this.dirty.get();
    }
}
