package no2.worldthreader.mixin.fixes.leash;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Leashable.class)
public interface LeashableMixin {

    @Inject(
            method = "tickLeash", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Leashable$LeashData;delayedLeashInfo:Lcom/mojang/datafixers/util/Either;")
    )
    private static <E extends Entity & Leashable> void handleTeleportedPlayer(ServerLevel serverLevel, E entity, CallbackInfo ci, @Local Leashable.LeashData leashData) {
        if (leashData.delayedLeashInfo == null && leashData.leashHolder instanceof ServerPlayer serverPlayer && serverPlayer.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION) {
            WorldThreadingManager worldThreadingManager = WorldThreadingManager.get(serverPlayer.level());
            if (worldThreadingManager != null && worldThreadingManager.isMultiThreadedPhase() && worldThreadingManager.wasAlive(serverPlayer.getUUID())) {
                leashData.delayedLeashInfo = Either.left(serverPlayer.getUUID());
            }
        }
    }

    @Redirect(
            method = "tickLeash(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;canInteractWithLevel()Z", ordinal = 1)
    )
    private static <E extends Entity & Leashable> boolean getThreadedIsAlive(Entity instance, @Local Leashable.LeashData leashData) {
        if (instance instanceof ServerPlayer serverPlayer && serverPlayer.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION) {
            WorldThreadingManager worldThreadingManager = WorldThreadingManager.get(serverPlayer.level());
            if (worldThreadingManager != null && worldThreadingManager.isMultiThreadedPhase()) {
                return worldThreadingManager.wasAlive(serverPlayer.getUUID());
            }
            return !serverPlayer.isDeadOrDying();
            //TODO this is wrong when worldthreader is disabled and the player dies after changing
            // dimension. In vanilla the leash breaks, with worldthreader it
            // probably doesn't. However, this issue is very minor and won't be worked on until users bring it up.
        }
        return instance.isAlive();
    }

    @ModifyConstant(
            method = "restoreLeashFromSave", constant = @Constant(intValue = 100)
    )
    private static int skipDestroyingLeashIfTeleportedPlayer(int constant, @Local(argsOnly = true) Leashable.LeashData leashData) {
        if (leashData.delayedLeashInfo != null && leashData.leashHolder instanceof ServerPlayer serverPlayer && serverPlayer.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION) {
            WorldThreadingManager worldThreadingManager = WorldThreadingManager.get(serverPlayer.level());
            if (worldThreadingManager != null && worldThreadingManager.wasAlive(serverPlayer.getUUID())) {
                return Integer.MAX_VALUE;
            }
        }
        return constant;
    }
}
