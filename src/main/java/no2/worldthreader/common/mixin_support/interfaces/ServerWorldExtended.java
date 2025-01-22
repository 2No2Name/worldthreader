package no2.worldthreader.common.mixin_support.interfaces;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import net.minecraft.server.level.ServerLevel;

public interface ServerWorldExtended {

    void worldthreader$receiveTeleportedEntity(ResourceKey<Level> source, TeleportedEntityInfo teleportedEntityInfo);

    void worldthreader$finishReceivingTeleportedEntities();

    void worldthreader$receiveFailedTeleport(TeleportedEntityInfo teleportedEntityInfo);

    void worldthreader$recoverFailedTeleports();

    TeleportedEntityInfo worldthreader$getCurrentlyArrivingEntityInfo();

    void worldthreader$setCurrentlyArrivingEntityInfo(TeleportedEntityInfo teleportedEntityInfo);

}
