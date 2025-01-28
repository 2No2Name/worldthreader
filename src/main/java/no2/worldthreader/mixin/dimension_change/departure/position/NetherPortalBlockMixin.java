package no2.worldthreader.mixin.dimension_change.departure.position;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import no2.worldthreader.common.thread.ThreadLocals;
import no2.worldthreader.common.tuples.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin implements Portal {


    @Inject(
            method = "getDimensionTransitionFromExit(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/BlockUtil$FoundRectangle;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/NetherPortalBlock;createDimensionTransition(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/BlockUtil$FoundRectangle;Lnet/minecraft/core/Direction$Axis;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;"
            ), cancellable = true
    )
    private static void storeNetherPortalAxisAndRelativePosition(Entity entity, BlockPos blockPos, BlockUtil.FoundRectangle foundRectangle, ServerLevel serverLevel, TeleportTransition.PostTeleportTransition postTeleportTransition, CallbackInfoReturnable<TeleportTransition> cir,
                                                                 @Local Direction.Axis portalAxis, @Local Vec3 relativeInPortalPos) {
        //If these 3 values are null, this is worldthreader's additional call for getting the relative position only upon departure without accessing the destination world
        if (foundRectangle == null && serverLevel == null && postTeleportTransition == null) {
            cir.setReturnValue(null);
            if (ThreadLocals.NETHER_PORTAL_POSITION_INFO.get() != null) {
                throw new IllegalStateException("Worldthreader: Cannot store nether portal position info as field is already set!");
            }
            ThreadLocals.NETHER_PORTAL_POSITION_INFO.set(new Pair<>(portalAxis, relativeInPortalPos));
        }
    }
}
