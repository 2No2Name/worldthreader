package no2.worldthreader.mixin.threading_compatibility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerTickRateManager.class)
public class ServerTickRateManagerMixin {


    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(
            method = {"setFrozen"}, at = @At("HEAD")
    )
    private void ensureSafe(boolean bl, CallbackInfo ci) {
        this.server.getAllLevels();
    }

    @Inject(
            method = {"stopStepping", "stopSprinting"}, at = @At("HEAD")
    )
    private void ensureSafe(CallbackInfoReturnable<Boolean> cir) {
        this.server.getAllLevels();
    }

    @Inject(
            method = {"requestGameToSprint", "stepGameIfPaused"}, at = @At("HEAD")
    )
    private void ensureSafe(int i, CallbackInfoReturnable<Boolean> cir) {
        this.server.getAllLevels();
    }

    @Inject(
            method = {"setTickRate"}, at = @At("HEAD")
    )
    private void ensureSafe(float f, CallbackInfo ci) {
        this.server.getAllLevels();
    }
}
