package no2.worldthreader.common.dimension_change;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.portal.TeleportTransition;
import no2.worldthreader.common.ServerWorldTicking;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import no2.worldthreader.init.ModGameRules;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DimensionChangeHelper {

    public static boolean isDummy(TeleportTransition teleportTransition) {
        //noinspection resource,ConstantValue
        return teleportTransition.postTeleportTransition() == null;
    }

    public static void expectDummy(TeleportTransition teleportTransition, String message) {
        if (!DimensionChangeHelper.isDummy(teleportTransition)) {
            throw new IllegalStateException("Worldthreader: " + message);
        }
    }

    public static boolean shouldConvertSelfToTeleportedEntityInfo(ServerLevel destination) {
        return WorldThreadingManager.isWrongThreadForWorld(destination);
    }

    public static TeleportTransition getNonPassengerDummyTeleportTarget(ServerLevel destination) {
        return new TeleportTransition(destination, null, null, 0.0f, 0.0f, false, false, null, null);
    }

    public static void nonPassengerArriveInWorld(TeleportedEntityInfo teleportedEntityInfo, Entity oldEntityObject, ServerLevel destination, ServerLevel source) {

        TeleportedEntityInfo previous = ((ServerWorldExtended) destination).worldthreader$arrivingEntityInfo();
        TeleportTransition teleportTransition;
        if (teleportedEntityInfo.entityTransition() != null) {
            teleportTransition = teleportedEntityInfo.entityTransition();
        } else {
            ((ServerWorldExtended) destination).worldthreader$setArrivingEntityInfo(teleportedEntityInfo);

            //Server players might be able to move after starting to teleport (packets being sent from the client, sent
            // before client knows about changing dimensions) This is why the entry position of the portal processor
            // must be updated to the original position.
            //Furthermore, using oldEntityObject.portalProcessor is not safe, as it might be null, since a moved player
            // will also be ticked (the network connection tick ticks the serverside player entity), removing the portal
            // processor if the player is no longer intersecting the portal.
            // Related to https://github.com/2No2Name/worldthreader/issues/12
            PortalProcessor portalProcessor = Objects.requireNonNull(teleportedEntityInfo.portalProcessor());
            portalProcessor.updateEntryPosition(teleportedEntityInfo.portalProcessorPos());

            teleportTransition = portalProcessor.getPortalDestination(source, oldEntityObject);
            ((ServerWorldExtended) destination).worldthreader$setArrivingEntityInfo(previous);
        }

        if (teleportTransition == null) {
            ((ServerWorldExtended) source).worldthreader$receiveFailedTeleport(teleportedEntityInfo);
            return;
        }

        Entity newEntity = arriveIntoWorld(teleportedEntityInfo, oldEntityObject, destination, source, teleportTransition);

        if (ServerWorldTicking.isMainWorld(destination) && destination.getGameRules().getBoolean(ModGameRules.TELEPORTED_ENTITY_ADDITIONAL_TICK.getKey())) {
            newEntity.tick();
            //Small todo: maybe use a collection and then tick all of the ones in the collection, avoids issue where the others didn't arrive yet and thus no interaction takes place (would only avoid this for the teleported ones though)
        }
    }

    public static @NotNull Entity arriveIntoWorld(TeleportedEntityInfo teleportedEntityInfo, Entity oldEntityObject, ServerLevel destination, ServerLevel source, TeleportTransition teleportTransition) {
        TeleportedEntityInfo previous = ((ServerWorldExtended) destination).worldthreader$arrivingEntityInfo();
        ((ServerWorldExtended) destination).worldthreader$setArrivingEntityInfo(teleportedEntityInfo);
        Entity newEntity;
        if (oldEntityObject instanceof ServerPlayer) {
            //ServerPlayer teleportation code is mostly separate from normal entity teleportation code, both in vanilla and worldthreader.
            //This should only be called when the player is a passenger. Normal player teleportation happens outside the multithreaded part of the tick.
            //Heavily modified method, essentially split into departure and arrival
            newEntity = oldEntityObject.teleport(teleportTransition);
        } else {
            //Heavily modified method, essentially split into departure and arrival
            newEntity = oldEntityObject.teleportCrossDimension(source, destination, teleportTransition);
        }
        ((ServerWorldExtended) destination).worldthreader$setArrivingEntityInfo(previous);

        if (newEntity == null) {
            throw new IllegalStateException("Worldthreader: Entity could not be placed after crossing dimensions: " + oldEntityObject);
        }

        ((EntityExtended) newEntity).worldthreader$onArrivedInServerWorld(destination.dimension(), source.dimension());
        return newEntity;
        //Small TODO trigger fabric-entity-events-v1.afterWorldChanged here
    }

    public static Entity restoreEntityInWorld(TeleportedEntityInfo entityInfo) {
        List<TeleportedEntityInfo> passengerInfos = entityInfo.passengers();

        List<Entity> passengers = new ArrayList<>(passengerInfos.size());
        for (TeleportedEntityInfo passenger : passengerInfos) {
            passengers.add(restoreEntityInWorld(passenger));
        }

        Entity entity = entityInfo.oldEntityObject();
        ((EntityExtended) entity).worldthreader$restoreEntity(entityInfo);

        for (Entity passenger : passengers) {
            passenger.startRiding(entity, true, false);
        }
        return entity;
    }
}
