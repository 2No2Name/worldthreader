package no2.worldthreader.mixin.threading_compatibility;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.gamerules.GameRule;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRuleCommand.class)
public class GameruleCommandMixin {

    @Inject(
            method = "setRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gamerules/GameRules;set(Lnet/minecraft/world/level/gamerules/GameRule;Ljava/lang/Object;Lnet/minecraft/server/MinecraftServer;)V")
    )
    private static <T> void getExclusiveGameruleAccess(CommandContext<CommandSourceStack> commandContext, GameRule<@NotNull T> gameRule, CallbackInfoReturnable<Integer> cir) {
        commandContext.getSource().getServer().getAllLevels();
    }
}
