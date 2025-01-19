package no2.worldthreader;

import no2.worldthreader.init.ModGameRules;
import net.fabricmc.api.ModInitializer;

public class WorldThreaderMod implements ModInitializer {

	public static final String MOD_ID = "worldthreader";

	@Override
	public void onInitialize() {
		ModGameRules.registerGameRules();
	}
}
