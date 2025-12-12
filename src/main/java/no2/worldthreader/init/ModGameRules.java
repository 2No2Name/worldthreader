package no2.worldthreader.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import no2.worldthreader.WorldThreaderMod;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.NotNull;

public class ModGameRules {

    public static GameRule<@NotNull Boolean> ACTIVE;
	public static final boolean INITIAL_TRUE = true;
	public static final boolean INITIAL_FALSE = false;
    public static GameRule<@NotNull Boolean> TELEPORTED_ENTITY_ADDITIONAL_TICK;
    public static GameRule<@NotNull Boolean> DEBUG;

	public static void registerGameRules() {
        ACTIVE = GameRuleBuilder.forBoolean(INITIAL_TRUE).category(GameRuleCategory.MISC).buildAndRegister(Identifier.fromNamespaceAndPath(WorldThreaderMod.MOD_ID, "active"));
        TELEPORTED_ENTITY_ADDITIONAL_TICK = GameRuleBuilder.forBoolean(INITIAL_FALSE).category(GameRuleCategory.MISC).buildAndRegister(Identifier.fromNamespaceAndPath(WorldThreaderMod.MOD_ID, "additional_entity_tick_after_teleport"));
        DEBUG = GameRuleBuilder.forBoolean(INITIAL_FALSE).category(GameRuleCategory.MISC).buildAndRegister(Identifier.fromNamespaceAndPath(WorldThreaderMod.MOD_ID, "debug"));
	}

	public static void syncDebugFlag(MinecraftServer server) {
		if (DEBUG != null) {
            WorldThreadingManager.DEBUG = server.getWorldData().getGameRules().get(DEBUG);
		}
	}
}
