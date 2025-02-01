package no2.worldthreader.mixin.threading_compatibility.commands;

import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.WhitelistCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(WhitelistCommand.class)
public class WhitelistCommandMixin {

    @Inject(
            method = {"addPlayers", "removePlayers"}, at = @At("HEAD")
    )
    private static void ensureSafe(CommandSourceStack commandSourceStack, Collection<GameProfile> collection, CallbackInfoReturnable<Integer> cir) {
        commandSourceStack.getServer().getAllLevels();
    }

    @Inject(
            method = {"disableWhitelist", "enableWhitelist"}, at = @At("HEAD")
    )
    private static void ensureSafe(CommandSourceStack commandSourceStack, CallbackInfoReturnable<Integer> cir) {
        commandSourceStack.getServer().getAllLevels();
    }
}
