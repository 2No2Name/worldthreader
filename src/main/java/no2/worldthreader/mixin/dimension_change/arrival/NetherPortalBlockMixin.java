package no2.worldthreader.mixin.dimension_change.arrival;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin implements Portal {

    //Medium TODO: getPortalDestination must be called with the old entity but then some checks like powder snow leather shoes for placement / Entity shape context could be wrong, as the armor/hand items are gone already
    /**
     * Fixes deadlock when an entity teleports using a nether portal to another dimension during the world tick.
     * Without this the source world would be accessed when arriving in the destination world on the destination's thread.
     * To prevent this the relative position in the source nether portal is calculated on departure and stored in the TeleportedEntityInfo.
     * On arrival the precalculated values are used instead of accessing the source world.
     */
    @Redirect(
            method = "getExitPortal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private BlockState avoidAccessingWrongWorld(Level departureWorld, BlockPos pos) {
        if (departureWorld instanceof ServerLevel serverLevel && WorldThreadingManager.isWrongThreadForWorld(serverLevel)) {
            //Code that is circumvented here was already evaluated during departure, results stored in TeleportedEntityInfo
            return Blocks.AIR.defaultBlockState();
        }
        return departureWorld.getBlockState(pos);
    }

    /**
     * Fixes deadlock when an entity teleports using a nether portal to another dimension during the world tick.
     * Without this the source world would be accessed when arriving in the destination world on the destination's thread.
     * To prevent this the relative position in the source nether portal is calculated on departure and stored in the TeleportedEntityInfo.
     * On arrival the precalculated values are used instead of accessing the source world.
     */
    @ModifyArg(
            method = "getExitPortal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/PortalForcer;createPortal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;)Ljava/util/Optional;")
    )
    private Direction.Axis restorePortalAxis(Direction.Axis portalAxis, @Local(argsOnly = true) ServerLevel targetWorld) {
        TeleportedEntityInfo currentlyArrivingEntity = ((ServerWorldExtended) targetWorld).worldthreader$arrivingEntityInfo();
        if (currentlyArrivingEntity != null && currentlyArrivingEntity.portalAxis() != null) {
            return currentlyArrivingEntity.portalAxis();
        }
        return portalAxis;
    }

    /**
     * Fixes deadlock when an entity teleports using a nether portal to another dimension during the world tick.
     * Without this the source world would be accessed when arriving in the destination world on the destination's thread.
     * To prevent this the relative position in the source nether portal is calculated on departure and stored in the TeleportedEntityInfo.
     * On arrival the precalculated values are used instead of accessing the source world.
     */
    @Redirect(
            method = "getDimensionTransitionFromExit(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/BlockUtil$FoundRectangle;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private static BlockState avoidAccessingWrongWorld2(Level departureWorld, BlockPos pos) {
        if (departureWorld instanceof ServerLevel serverLevel && WorldThreadingManager.isWrongThreadForWorld(serverLevel)) {
            //Code that is circumvented here was already evaluated during departure, results stored in TeleportedEntityInfo
            return Blocks.AIR.defaultBlockState();
        }
        return departureWorld.getBlockState(pos);
    }

    @ModifyArg(
            method = "getDimensionTransitionFromExit(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/BlockUtil$FoundRectangle;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/NetherPortalBlock;createDimensionTransition(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/BlockUtil$FoundRectangle;Lnet/minecraft/core/Direction$Axis;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;")
    )
    private static Direction.Axis restorePortalAxis2(Direction.Axis portalAxis, @Local(argsOnly = true) ServerLevel targetWorld) {
        TeleportedEntityInfo currentlyArrivingEntity = targetWorld == null ? null : ((ServerWorldExtended) targetWorld).worldthreader$arrivingEntityInfo();
        if (currentlyArrivingEntity != null && currentlyArrivingEntity.portalAxis() != null) {
            return currentlyArrivingEntity.portalAxis();
        }
        return portalAxis;
    }

    @ModifyArg(
            method = "getDimensionTransitionFromExit(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/BlockUtil$FoundRectangle;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/NetherPortalBlock;createDimensionTransition(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/BlockUtil$FoundRectangle;Lnet/minecraft/core/Direction$Axis;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;")
    )
    private static Vec3 restoreInPortalPos(Vec3 inPortalPos, @Local(argsOnly = true) ServerLevel targetWorld) {
        TeleportedEntityInfo currentlyArrivingEntity = targetWorld == null ? null : ((ServerWorldExtended) targetWorld).worldthreader$arrivingEntityInfo();
        if (currentlyArrivingEntity != null && currentlyArrivingEntity.inPortalPos() != null) {
            return currentlyArrivingEntity.inPortalPos();
        }
        return inPortalPos;
    }
}
