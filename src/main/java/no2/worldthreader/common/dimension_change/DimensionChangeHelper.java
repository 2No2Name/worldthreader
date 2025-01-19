package no2.worldthreader.common.dimension_change;

import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.portal.TeleportTransition;
import no2.worldthreader.common.ServerWorldTicking;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import no2.worldthreader.init.ModGameRules;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DimensionChangeHelper {

    public static final ConcurrentHashMap<ResourceKey<Level>, TeleportTransition> NON_PASSENGER_DUMMY_TARGETS = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<TeleportTransition, ResourceKey<Level>> NON_PASSENGER_DUMMY_TARGETS_REVERSE = new ConcurrentHashMap<>();

    public static ResourceKey<Level> getDestinationFromNonPassengerDummyElseNull(TeleportTransition teleportTransition) {
        return NON_PASSENGER_DUMMY_TARGETS_REVERSE.get(teleportTransition);
    }

    public static boolean isDummy(TeleportTransition teleportTransition) {
        //noinspection resource,ConstantValue
        return teleportTransition.postTeleportTransition() == null;
    }

    public static void expectDummy(TeleportTransition teleportTransition) {
        if (!DimensionChangeHelper.isDummy(teleportTransition)) {
            throw new IllegalStateException("Worldthreader: Invalid caller for cross dimensional passenger teleport!");
        }
    }

    public static TeleportTransition getNonPassengerDummyTeleportTarget(ServerLevel destination) {
        return NON_PASSENGER_DUMMY_TARGETS.computeIfAbsent(
                destination.dimension(),
                key -> {
                    TeleportTransition teleportTarget = new TeleportTransition(destination, null, null, 0.0f, 0.0f, false, false, null, null);
                    NON_PASSENGER_DUMMY_TARGETS_REVERSE.put(teleportTarget, key);
                    return teleportTarget;
        });
    }

    public static void nonPassengerArriveInWorld(TeleportedEntityInfo teleportedEntityInfo, Entity oldEntityObject, ServerLevel destination, ServerLevel source) {

        TeleportedEntityInfo previous = ((ServerWorldExtended) destination).worldthreader$getCurrentlyArrivingEntityInfo();
        ((ServerWorldExtended) destination).worldthreader$setCurrentlyArrivingEntityInfo(teleportedEntityInfo);
        TeleportTransition teleportTransition = Objects.requireNonNull(oldEntityObject.portalProcess).getPortalDestination(source, oldEntityObject);
        ((ServerWorldExtended) destination).worldthreader$setCurrentlyArrivingEntityInfo(previous);

        if (teleportTransition == null) {
            ((ServerWorldExtended) source).worldthreader$receiveFailedTeleport(teleportedEntityInfo);
            return;
        }

        Entity newEntity = arriveIntoWorld(teleportedEntityInfo, oldEntityObject, destination, source, teleportTransition);

        if (ModGameRules.SHOULD_TICK_ENTITY_AFTER_TELEPORT && ServerWorldTicking.isMainWorld(destination)) {
            newEntity.tick();
            //TODO maybe use a collection and then tick all of the ones in the collection, avoids issue where the others didn't arrive yet and thus no interaction takes place (would only avoid this for the teleported ones though)
        }
    }

    public static @NotNull Entity arriveIntoWorld(TeleportedEntityInfo teleportedEntityInfo, Entity oldEntityObject, ServerLevel destination, ServerLevel source, TeleportTransition teleportTransition) {
        TeleportedEntityInfo previous = ((ServerWorldExtended) destination).worldthreader$getCurrentlyArrivingEntityInfo();
        ((ServerWorldExtended) destination).worldthreader$setCurrentlyArrivingEntityInfo(teleportedEntityInfo);
        //Heavily modified method, essentially split into departure and arrival
        Entity newEntity = oldEntityObject.teleportCrossDimension(destination, teleportTransition);
        ((ServerWorldExtended) destination).worldthreader$setCurrentlyArrivingEntityInfo(previous);

        if (newEntity == null) {
            throw new IllegalStateException("Worldthreader: Entity could not be placed after crossing dimensions: " + oldEntityObject);
        }

        ((EntityExtended) newEntity).worldthreader$onArrivedInServerWorld(destination.dimension(), source.dimension());
        return newEntity;
    }

    public static void restoreEntityInWorld(TeleportedEntityInfo entityInfo) {
        Entity entity = entityInfo.oldEntityObject();
        ((EntityExtended) entity).worldthreader$restoreEntity(entityInfo);
        if (!entity.isRemoved()) { //Avoid adding entities that were removed for another reason, e.g. falling sand that landed or mobs that died
            ((ServerLevel) entity.level()).addWithUUID(entity);
        }
    }

    public static void requestEnderPearlTeleportFromDestinationWorld(ThrownEnderpearl thrownEnderpearl, Entity entityFromOtherWorld) {
        //TODO implement check in a threadsafe way, e.g. delay it (entity.canUsePortal), treat this as a request to teleport
        throw new UnsupportedOperationException("Cross Dimension Enderpearling not implemented yet!");
    }
}
