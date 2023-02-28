package _2no2name.worldthreader.init;

import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import _2no2name.worldthreader.gamerule.BoolRule;
import net.minecraft.world.GameRules;

public class ModGameRules {

	public static BoolRule ACTIVE;
	public static BoolRule TELEPORTED_ENTITY_ADDITIONAL_TICK;
	public static boolean INITIAL = true;

	public static boolean SHOULD_TICK_ENTITY_AFTER_TELEPORT;

	public static void registerGameRules() {
		ACTIVE = BoolRule.builder("active", GameRules.Category.MISC).setInitial(INITIAL)
				.setCallback((server, value) -> ((MinecraftServerExtended) server).setThreadingEnabled(value.get())).build();
		TELEPORTED_ENTITY_ADDITIONAL_TICK = BoolRule.builder("teleported_entity_additional_tick", GameRules.Category.MISC).setInitial(INITIAL)
				.setCallback((server, value) -> server.execute(() -> SHOULD_TICK_ENTITY_AFTER_TELEPORT = value.get())).build();
	}

}
