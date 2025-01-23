package no2.worldthreader.mixin.threading_compatibility;

import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.BanPlayerCommands;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(BanPlayerCommands.class)
public class BanPlayerCommandsMixin {

    @Inject(
            method = "banPlayers", at = @At("HEAD")
    )
    private static void ensureSafe(CommandSourceStack commandSourceStack, Collection<GameProfile> collection, @Nullable Component component, CallbackInfoReturnable<Integer> cir) {
        commandSourceStack.getServer().getAllLevels();
    }
}
