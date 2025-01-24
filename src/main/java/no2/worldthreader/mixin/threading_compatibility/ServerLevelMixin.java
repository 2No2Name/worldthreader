package no2.worldthreader.mixin.threading_compatibility;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(
            method = "setDefaultSpawnPos(Lnet/minecraft/core/BlockPos;F)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/WritableLevelData;setSpawn(Lnet/minecraft/core/BlockPos;F)V")
    )
    private void ensureSafe(BlockPos blockPos, float f, CallbackInfo ci) {
        this.server.getAllLevels();
    }
}
