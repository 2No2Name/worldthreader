package no2.worldthreader.common.dimension_change;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record TeleportedEntityInfo(
        @NotNull Entity oldEntityObject,
        @Nullable CompoundTag nbtCompound, //ONLY NULL FOR SERVER PLAYER
        @Nullable TeleportTransition entityTransition,
        @NotNull PositionMoveRotation positionMoveRotation,
        @Nullable PortalProcessor portalProcessor,
        @Nullable BlockPos portalProcessorPos,
        @Nullable Direction.Axis portalAxis,
        @Nullable Vec3 inPortalPos,
        @NotNull List<TeleportedEntityInfo> passengers
) {
    public TeleportedEntityInfo(Entity oldEntityObject,
                                @Nullable CompoundTag nbtCompound, //ONLY NULL FOR SERVER PLAYER
                                @Nullable TeleportTransition entityTransition,
                                @Nullable PortalProcessor portalProcessor,
                                @Nullable Direction.Axis portalAxis,
                                @Nullable Vec3 inPortalPos,
                                @NotNull List<TeleportedEntityInfo> passengers) {
        this(oldEntityObject, nbtCompound, entityTransition, PositionMoveRotation.of(oldEntityObject), portalProcessor, portalProcessor == null ? null : portalProcessor.getEntryPosition(), portalAxis, inPortalPos, passengers);

    }

    public TeleportedEntityInfo(Entity oldEntityObject,
                                @Nullable CompoundTag nbtCompound, //ONLY NULL FOR SERVER PLAYER
                                @Nullable TeleportTransition entityTransition,
                                @NotNull PositionMoveRotation positionMoveRotation,
                                @Nullable PortalProcessor portalProcessor,
                                @Nullable BlockPos portalProcessorPos,
                                @Nullable Direction.Axis portalAxis,
                                @Nullable Vec3 inPortalPos,
                                @NotNull List<TeleportedEntityInfo> passengers) {
        this.oldEntityObject = oldEntityObject;
        this.nbtCompound = nbtCompound;
        this.entityTransition = entityTransition;
        this.positionMoveRotation = positionMoveRotation;
        this.portalProcessor = portalProcessor;
        this.portalProcessorPos = portalProcessorPos;
        this.portalAxis = portalAxis;
        this.inPortalPos = inPortalPos;
        this.passengers = passengers;

        if (this.nbtCompound == null && !(this.oldEntityObject instanceof ServerPlayer)) {
            throw new IllegalStateException("Worldthreader: Null nbt only allowed for server player entity!");
        }
        if (this.entityTransition != null && DimensionChangeHelper.isDummy(this.entityTransition)) {
            throw new IllegalStateException("Worldthreader: Dummy transition not allowed!");
        }
    }
}
