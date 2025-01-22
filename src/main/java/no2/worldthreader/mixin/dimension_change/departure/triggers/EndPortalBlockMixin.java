package no2.worldthreader.mixin.dimension_change.departure.triggers;

import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.portal.TeleportTransition;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndPortalBlock.class)
public abstract class EndPortalBlockMixin implements Portal {
//TODO players might teleport directly to nether(respawn anchor)

    /**
     * Danger Zone: Accessing worlds from multiple threads. The following mixins ensure that (if no other mods interfere)
     * the world's mutable data is only accessed from its own thread.
     */
    @Redirect(
            method = "getPortalDestination(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;")
    )
    private ServerLevel getWorldWithoutExclusiveAccess(MinecraftServer server, ResourceKey<Level> key) {
        return ((MinecraftServerExtended) server).worldthreader$getLevelUnsynchronized(key);
    }

    @Inject(
            method = "getPortalDestination(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(
                    value = "INVOKE_ASSIGN", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"
            ), cancellable = true
    )
    private void handleOffthreadTeleport(ServerLevel originWorld, Entity entity, BlockPos pos, CallbackInfoReturnable<TeleportTransition> cir, @Local(ordinal = 1) ServerLevel targetWorld) {
        if (targetWorld != null && WorldThreadingManager.needsExclusiveAccessForWorld(targetWorld)) {
            cir.setReturnValue(DimensionChangeHelper.getNonPassengerDummyTeleportTarget(targetWorld));
        }
    }
}
