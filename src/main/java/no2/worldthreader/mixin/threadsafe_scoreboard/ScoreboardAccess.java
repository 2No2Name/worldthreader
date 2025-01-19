package no2.worldthreader.mixin.threadsafe_scoreboard;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

@Mixin(Scoreboard.class)
public interface ScoreboardAccess {
    @Accessor("objectivesByName")
    Object2ObjectMap<String, Objective> getObjectivesByName();
}
