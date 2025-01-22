package no2.worldthreader.mixin.dimension_change.arrival;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @WrapOperation(
            method = "sendLevelInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;")
    )
    private ServerLevel getOverworldUnsafeIfArrivingEntityPhase(MinecraftServer instance, Operation<ServerLevel> original, @Local(argsOnly = true) ServerLevel serverLevel) {
        if (((ServerWorldExtended) serverLevel).worldthreader$arrivingEntityInfo() != null) {
            return ((MinecraftServerExtended) instance).worldthreader$getLevelUnsynchronized(Level.OVERWORLD);
        }
        return original.call(instance);
    }
}
