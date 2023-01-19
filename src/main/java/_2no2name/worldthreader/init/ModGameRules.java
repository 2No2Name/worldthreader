package _2no2name.worldthreader.init;

import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import _2no2name.worldthreader.gamerule.BoolRule;
import net.minecraft.world.GameRules;

public class ModGameRules {

	public static BoolRule ACTIVE;
	public static boolean INITIAL = true;

	public static void registerGameRules() {
		ACTIVE = BoolRule.builder("active", GameRules.Category.MISC).setInitial(INITIAL)
				.setCallback((server, value) -> ((MinecraftServerExtended) server).setThreadingEnabled(value.get())).build();
	}

}
