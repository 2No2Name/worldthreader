package no2.worldthreader.mixin.threading_compatibility.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.WeatherCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeatherCommand.class)
public class WeatherCommandMixin {

    @Inject(
            method = {"setRain", "setClear", "setThunder"}, at = @At("HEAD")
    )
    private static void ensureSafe(CommandSourceStack commandSourceStack, int i, CallbackInfoReturnable<Integer> cir) {
        commandSourceStack.getServer().getAllLevels();
    }
}
