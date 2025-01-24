package no2.worldthreader.mixin.threading_compatibility.dedicated_server_console;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.server.dedicated.DedicatedServer$1")
public class DedicatedServer$ConsoleHandlerThreadMixin {

    @Redirect(
            method = "run",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServer;createCommandSourceStack()Lnet/minecraft/commands/CommandSourceStack;")
    )
    private CommandSourceStack getNull(DedicatedServer instance) {
        return null;
    }
}
