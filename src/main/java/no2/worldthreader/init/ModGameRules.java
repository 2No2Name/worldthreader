package no2.worldthreader.init;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import no2.worldthreader.WorldThreaderMod;
import no2.worldthreader.common.thread.WorldThreadingManager;
import no2.worldthreader.gamerule.BoolRule;

public class ModGameRules {

	public static BoolRule ACTIVE;
	public static final boolean INITIAL_TRUE = true;
	public static final boolean INITIAL_FALSE = false;
	public static BoolRule TELEPORTED_ENTITY_ADDITIONAL_TICK;
	public static BoolRule DEBUG;

	public static void registerGameRules() {
		try {
			ACTIVE = BoolRule.builder("Active", GameRules.Category.MISC).setInitial(INITIAL_TRUE).build();
			TELEPORTED_ENTITY_ADDITIONAL_TICK = BoolRule.builder("AdditionalEntityTickAfterTeleport", GameRules.Category.MISC).setInitial(INITIAL_FALSE).build();
			DEBUG = BoolRule.builder("Debug", GameRules.Category.MISC).setInitial(INITIAL_FALSE)
					.setCallback((server, value) -> {
						if (WorldThreadingManager.DEBUG == value.get()) {
							return;
						}
						server.execute(() -> WorldThreadingManager.DEBUG = value.get());
						CommandSourceStack commandSourceStack = server.createCommandSourceStack();
						if (value.get()) {
							commandSourceStack.sendSuccess(() -> Component.literal("Worldthreader: Starting debug info logging!"), true);
						} else {
							commandSourceStack.sendSuccess(() -> Component.literal("Worldthreader: Stopping debug info logging!"), true);
						}
					}).build();
		} catch (Throwable exception) {
			WorldThreaderMod.LOGGER.error("Worldthreader: Could not register gamerules. Using default values!");
		}
	}

	public static void syncDebugFlag(MinecraftServer server) {
		if (DEBUG != null) {
			WorldThreadingManager.DEBUG = server.getGameRules().getBoolean(DEBUG.getKey());
		}
	}
}
