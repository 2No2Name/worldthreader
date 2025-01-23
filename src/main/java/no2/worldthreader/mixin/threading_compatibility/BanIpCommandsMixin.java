package no2.worldthreader.mixin.threading_compatibility;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.BanIpCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BanIpCommands.class)
public class BanIpCommandsMixin {

    @Inject(
            method = "banIp", at = @At("HEAD")
    )
    private static void ensureSafe(CommandSourceStack commandSourceStack, String string, Component component, CallbackInfoReturnable<Integer> cir) {
        commandSourceStack.getServer().getAllLevels();
    }
}
