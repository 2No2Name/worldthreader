package no2.worldthreader;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import no2.worldthreader.common.mixin_support.interfaces.BeforeThreadingInitialization;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.init.ModGameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldThreaderMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("worldthreader");

	public static final String MOD_ID = "worldthreader";

	@Override
	public void onInitialize() {
		ModGameRules.registerGameRules();

        ServerLevelEvents.LOAD.register((server, world) -> ((MinecraftServerExtended) server).worldthreader$onLevelAddedOrRemoved());
        ServerLevelEvents.UNLOAD.register((server, world) -> ((MinecraftServerExtended) server).worldthreader$onLevelAddedOrRemoved());
	}

	public static void initializeBeforeThreading(MinecraftServer server) {
		ModGameRules.syncDebugFlag(server);
		((BeforeThreadingInitialization) Blocks.CARVED_PUMPKIN).worldthreader$initBeforeThreading(server);
		((BeforeThreadingInitialization) Blocks.END_PORTAL_FRAME).worldthreader$initBeforeThreading(server);
		((BeforeThreadingInitialization) Blocks.WITHER_SKELETON_SKULL).worldthreader$initBeforeThreading(server);
		((BeforeThreadingInitialization) Blocks.REDSTONE_TORCH).worldthreader$initBeforeThreading(server);
	}
}
