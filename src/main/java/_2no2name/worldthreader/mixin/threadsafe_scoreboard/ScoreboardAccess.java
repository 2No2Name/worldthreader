package _2no2name.worldthreader.mixin.threadsafe_scoreboard;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(Scoreboard.class)
public interface ScoreboardAccess {
    @Accessor("objectives")
    Map<String, ScoreboardObjective> getObjectives();
}
