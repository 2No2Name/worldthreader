package no2.worldthreader.gamerule;

import no2.worldthreader.WorldThreaderMod;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;

public abstract class GameRule<T extends GameRules.Value<T>> {

	private final GameRules.Key<T> key;
	private final GameRules.Type<T> rule;

	public GameRule(String name, GameRules.Category category, GameRules.Type<T> rule) {
		this.key = GameRuleRegistry.register(WorldThreaderMod.MOD_ID + "_" + name, category, rule);
		this.rule = rule;
	}

	public GameRules.Key<T> getKey() {
		return this.key;
	}

	public GameRules.Type<T> getRule() {
		return this.rule;
	}

}
