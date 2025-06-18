package no2.worldthreader.mixin.dimension_change.departure.triggers.ender_pearl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {


    @Shadow
    public abstract ServerLevel level();

    @WrapOperation(
            method = "registerAndUpdateEnderPearlTicket",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;registerEnderPearl(Lnet/minecraft/world/entity/projectile/ThrownEnderpearl;)V")
    )
    private void handleOffthread(ServerPlayer instance, ThrownEnderpearl thrownEnderpearl, Operation<Void> original) {
        if (WorldThreadingManager.isWrongThreadForWorld(this.level())) {
            return; //Ender pearl already updates this on acquiring the owner
        }
        original.call(instance, thrownEnderpearl);
    }
}
