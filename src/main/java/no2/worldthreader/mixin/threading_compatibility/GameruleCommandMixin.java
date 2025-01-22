package no2.worldthreader.mixin.threading_compatibility;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRuleCommand.class)
public class GameruleCommandMixin {

    @Inject(
            method = "setRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules$Value;setFromArgument(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;)V")
    )
    private static void getExclusiveGameruleAccess(CommandContext<CommandSourceStack> commandContext, GameRules.Key<?> key, CallbackInfoReturnable<Integer> cir) {
        commandContext.getSource().getServer().getAllLevels();
    }
}
