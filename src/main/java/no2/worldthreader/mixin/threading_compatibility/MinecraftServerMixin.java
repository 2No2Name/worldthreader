package no2.worldthreader.mixin.threading_compatibility;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerExtended {

    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Redirect(
            method = "getGameRules()Lnet/minecraft/world/level/GameRules;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;")
    )
    private ServerLevel getOverworldDirect(MinecraftServer instance) {
        return this.worldthreader$getLevelUnsynchronized(Level.OVERWORLD);
    }
}
