package no2.worldthreader.mixin.dimension_change.player.instance_swap;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import no2.worldthreader.common.mixin_support.interfaces.TransparentServerPlayerSwapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {


    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "HEAD")
    )
    private void check(TeleportTransition teleportTransition, CallbackInfoReturnable<ServerPlayer> cir) {
        if (DimensionChangeHelper.isDummy(teleportTransition)) {
            System.out.println("Player teleported with dummy transition!");
        }
    }

    @ModifyReturnValue(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "RETURN", ordinal = 2)
    )
    private ServerPlayer swapPlayerWithNewInstance(ServerPlayer original, @Local(ordinal = 0) ServerLevel level1, @Local(ordinal = 1) ServerLevel level2) {
        if (level1 == level2) {
            throw new AssertionError("Worldthreader Mixin placed at incorrect position in ServerPlayer.teleport!");
        }
        ServerPlayer serverPlayer = ((TransparentServerPlayerSwapper) original.connection).worldthreader$swapPlayerWithNewCopy(original);
        if (!original.isRemoved()) {
            throw new IllegalStateException("Worldthreader: Original player must be removed after replacement with new copy!");
        }
        return serverPlayer;
    }
}
