package no2.worldthreader;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.level.block.Blocks;
import no2.worldthreader.common.mixin_support.interfaces.BeforeThreadingInitialization;
import no2.worldthreader.init.ModGameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldThreaderMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("worldthreader");

	public static final String MOD_ID = "worldthreader";

	@Override
	public void onInitialize() {
		ModGameRules.registerGameRules();
	}

	public static void initializeBeforeThreading() {
		((BeforeThreadingInitialization) Blocks.CARVED_PUMPKIN).worldthreader$initBeforeThreading();
		((BeforeThreadingInitialization) Blocks.END_PORTAL_FRAME).worldthreader$initBeforeThreading();
		((BeforeThreadingInitialization) Blocks.WITHER_SKELETON_SKULL).worldthreader$initBeforeThreading();
	}
}
