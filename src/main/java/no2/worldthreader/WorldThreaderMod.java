package no2.worldthreader;

import no2.worldthreader.init.ModGameRules;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldThreaderMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("worldthreader");

	public static final String MOD_ID = "worldthreader";

	@Override
	public void onInitialize() {
		ModGameRules.registerGameRules();
	}
}
