package no2.worldthreader.init;

import net.minecraft.world.level.GameRules;
import no2.worldthreader.WorldThreaderMod;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.gamerule.BoolRule;

public class ModGameRules {

	public static BoolRule ACTIVE;
	public static final boolean INITIAL_TRUE = true;
	public static final boolean INITIAL_FALSE = false;
	public static BoolRule TELEPORTED_ENTITY_ADDITIONAL_TICK;
	public static boolean SHOULD_TICK_ENTITY_AFTER_TELEPORT = INITIAL_FALSE;

	public static void registerGameRules() {
		try {
			ACTIVE = BoolRule.builder("Active", GameRules.Category.MISC).setInitial(INITIAL_TRUE)
					.setCallback((server, value) -> ((MinecraftServerExtended) server).worldthreader$setThreadingEnabled(value.get())).build();
			TELEPORTED_ENTITY_ADDITIONAL_TICK = BoolRule.builder("AdditionalEntityTickAfterTeleport", GameRules.Category.MISC).setInitial(INITIAL_FALSE)
					.setCallback((server, value) -> server.execute(() -> SHOULD_TICK_ENTITY_AFTER_TELEPORT = value.get())).build();
		} catch (Throwable exception) {
			WorldThreaderMod.LOGGER.error("Worldthreader: Could not register gamerules. Using default values!");
		}
	}
}
