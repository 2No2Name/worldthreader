package no2.worldthreader.mixin.threading_compatibility;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.WorldBorderCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldBorderCommand.class)
public class WorldBorderCommandMixin {
    @Inject(
            method = {"setSize"}, at = @At("HEAD")
    )
    private static void ensureSafe(CommandSourceStack commandSourceStack, double d, long l, CallbackInfoReturnable<Integer> cir) {
        commandSourceStack.getServer().getAllLevels();
    }

    @Inject(
            method = {"setCenter"}, at = @At("HEAD")
    )
    private static void ensureSafe(CommandSourceStack commandSourceStack, Vec2 vec2, CallbackInfoReturnable<Integer> cir) {
        commandSourceStack.getServer().getAllLevels();
    }

    @Inject(
            method = {"setWarningDistance", "setWarningTime"}, at = @At("HEAD")
    )
    private static void ensureSafe(CommandSourceStack commandSourceStack, int i, CallbackInfoReturnable<Integer> cir) {
        commandSourceStack.getServer().getAllLevels();
    }

    @Inject(
            method = {"setDamageAmount", "setDamageBuffer"}, at = @At("HEAD")
    )
    private static void ensureSafe(CommandSourceStack commandSourceStack, float f, CallbackInfoReturnable<Integer> cir) {
        commandSourceStack.getServer().getAllLevels();
    }

    @Redirect(
            method = "getSize",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;")
    )
    private static ServerLevel getOverworldDirect(MinecraftServer instance) {
        return ((MinecraftServerExtended) instance).worldthreader$getLevelUnsynchronized(Level.OVERWORLD);
    }
}
