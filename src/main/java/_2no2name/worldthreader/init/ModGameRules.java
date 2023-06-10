package _2no2name.worldthreader.init;

import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import _2no2name.worldthreader.gamerule.BoolRule;
import net.minecraft.world.GameRules;

public class ModGameRules {

	public static BoolRule ACTIVE;
	public static BoolRule TELEPORTED_ENTITY_ADDITIONAL_TICK;
	public static final boolean INITIAL_TRUE = true;
	public static final boolean INITIAL_FALSE = false;
	public static BoolRule ENTITIES_CREATE_NETHER_PORTALS;
	public static boolean SHOULD_TICK_ENTITY_AFTER_TELEPORT = INITIAL_TRUE;
	public static boolean SHOULD_CREATE_NETHER_PORTALS_FOR_ALL_ENTITIES = INITIAL_FALSE;

	public static void registerGameRules() {
		ACTIVE = BoolRule.builder("Active", GameRules.Category.MISC).setInitial(INITIAL_TRUE)
				.setCallback((server, value) -> ((MinecraftServerExtended) server).setThreadingEnabled(value.get())).build();
		TELEPORTED_ENTITY_ADDITIONAL_TICK = BoolRule.builder("AdditionalEntityTickAfterTeleport", GameRules.Category.MISC).setInitial(INITIAL_TRUE)
				.setCallback((server, value) -> server.execute(() -> SHOULD_TICK_ENTITY_AFTER_TELEPORT = value.get())).build();
		ENTITIES_CREATE_NETHER_PORTALS = BoolRule.builder("CreateNetherPortalsForAllEntities", GameRules.Category.MISC).setInitial(INITIAL_FALSE)
				.setCallback((server, value) -> server.execute(() -> SHOULD_CREATE_NETHER_PORTALS_FOR_ALL_ENTITIES = value.get())).build();
	}

}
