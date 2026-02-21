package no2.worldthreader.mixin.dimension_change;


import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import no2.worldthreader.common.WorldThreaderTickPhase;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements ServerWorldExtended {

    @Unique
    private final Map<ResourceKey<Level>, ArrayList<TeleportedEntityInfo>> receivedEntities = new ConcurrentHashMap<>();
    @Unique
    private final Set<TeleportedEntityInfo> failedTeleports = new ConcurrentHashMap<TeleportedEntityInfo, Object>().keySet(new Object());
    @Unique
    private TeleportedEntityInfo currentlyArrivingEntity;
    @Unique
    private TeleportedEntityInfo currentlyDepartingPassenger;
    @Unique
    private WorldThreaderTickPhase tickPhase = WorldThreaderTickPhase.NONE;

    protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, bl, bl2, l, i);
    }

    @Shadow
    @NotNull
    public abstract MinecraftServer getServer();

    @Override
    public void worldthreader$receiveTeleportedEntity(ResourceKey<Level> source, TeleportedEntityInfo teleportedEntityInfo) {
        ArrayList<TeleportedEntityInfo> teleportedEntities = this.receivedEntities.computeIfAbsent(source, (ResourceKey<Level> s) -> new ArrayList<>());
        teleportedEntities.add(teleportedEntityInfo);
    }

    @Override
    public void worldthreader$finishReceivingTeleportedEntities(Consumer<Entity> entityAdditionalTickConsumer) {
        Set<ResourceKey<Level>> levelKeys = this.getServer().levelKeys();
        for (ResourceKey<Level> source : levelKeys) {
            ArrayList<TeleportedEntityInfo> teleportedEntityList = this.receivedEntities.remove(source);
            if (teleportedEntityList != null) {
                for (TeleportedEntityInfo teleportedEntity : teleportedEntityList) {
                    try {
                        DimensionChangeHelper.nonPassengerArriveInWorld(teleportedEntity, teleportedEntity.oldEntityObject(), (ServerLevel) (Object) this, (ServerLevel) teleportedEntity.oldEntityObject().level(), entityAdditionalTickConsumer);
                    } catch (Exception e) {
                        throw new IllegalStateException("Worldthreader: Failed to receive teleported entity: " + teleportedEntity + " in dimension " + this.dimension() + "!", e);
                    }
                }
            }
        }
    }

    @Override
    public void worldthreader$receiveFailedTeleport(TeleportedEntityInfo teleportedEntityInfo) {
        this.failedTeleports.add(teleportedEntityInfo);
    }


    @Override
    public void worldthreader$recoverFailedTeleports() {
        if (!this.failedTeleports.isEmpty()) {
            for (TeleportedEntityInfo teleportedEntity : this.failedTeleports) {
                DimensionChangeHelper.restoreEntityInWorld(teleportedEntity);
            }
            this.failedTeleports.clear();
        }
    }

    @Override
    public TeleportedEntityInfo worldthreader$arrivingEntityInfo() {
        return this.currentlyArrivingEntity;
    }

    @Override
    public void worldthreader$setArrivingEntityInfo(TeleportedEntityInfo teleportedEntityInfo) {
        this.currentlyArrivingEntity = teleportedEntityInfo;
    }

    @Override
    public TeleportedEntityInfo worldthreader$removeDepartingEntityInfo() {
        TeleportedEntityInfo entityInfo = this.currentlyDepartingPassenger;
        this.currentlyDepartingPassenger = null;
        return entityInfo;
    }

    @Override
    public void worldthreader$putDepartingPassengerEntityInfo(TeleportedEntityInfo teleportedEntityInfo) {
        if (this.currentlyDepartingPassenger != null) {
            throw new IllegalStateException("Worldthreader: Another entity is already departing from this level!");
        }
        this.currentlyDepartingPassenger = teleportedEntityInfo;
    }

    @Override
    public WorldThreaderTickPhase worldthreader$getTickPhase() {
        return tickPhase;
    }

    @Override
    public void worldthreader$setTickPhase(WorldThreaderTickPhase tickPhase) {
        this.tickPhase = tickPhase;
    }
}
