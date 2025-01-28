package no2.worldthreader.mixin.dimension_change.arrival;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Redirect(
            method = "sendLevelInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;")
    )
    private ServerLevel getOverworldUnsafeIfArrivingEntityPhase(MinecraftServer instance, @Local(argsOnly = true) ServerLevel serverLevel) {
        if (WorldThreadingManager.isPlacingReceivedTeleports(serverLevel)) {
            return ((MinecraftServerExtended) instance).worldthreader$getLevelUnsynchronized(Level.OVERWORLD);
        }
        return instance.overworld();
    }
}
