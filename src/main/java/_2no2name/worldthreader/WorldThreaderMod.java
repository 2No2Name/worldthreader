package _2no2name.worldthreader;

import _2no2name.worldthreader.init.ModGameRules;
import net.fabricmc.api.ModInitializer;

public class WorldThreaderMod implements ModInitializer {

	public static final String MOD_ID = "worldthreader";

	@Override
	public void onInitialize() {
		ModGameRules.registerGameRules();
	}
}
