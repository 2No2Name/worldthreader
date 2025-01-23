package no2.worldthreader.mixin.threading_compatibility;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntitySelector.class)
public class EntitySelectorMixin {

    @Inject(
            method = "findPlayers(Lnet/minecraft/commands/CommandSourceStack;)Ljava/util/List;",
            at = @At(value = "HEAD")
    )
    private void ensureSafe(CommandSourceStack commandSourceStack, CallbackInfoReturnable<List<ServerPlayer>> cir) {
        commandSourceStack.getServer().getAllLevels();
    }
}
