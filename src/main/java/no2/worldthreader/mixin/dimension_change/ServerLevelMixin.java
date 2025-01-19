package no2.worldthreader.mixin.dimension_change;


import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements ServerWorldExtended {

    @Unique
    private final Map<ServerLevel, ArrayList<TeleportedEntityInfo>> receivedEntities = new ConcurrentHashMap<>();
    @Unique
    private final Set<TeleportedEntityInfo> failedTeleports = new ConcurrentHashMap<TeleportedEntityInfo, Object>().keySet(new Object());
    @Unique
    private TeleportedEntityInfo currentlyArrivingEntity;

    protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, bl, bl2, l, i);
    }

    @Shadow
    @NotNull
    public abstract MinecraftServer getServer();

    @Override
    public void worldthreader$receiveTeleportedEntity(ServerLevel source, TeleportedEntityInfo teleportedEntityInfo) {
        ArrayList<TeleportedEntityInfo> teleportedEntities = this.receivedEntities.computeIfAbsent(source, (ServerLevel s) -> new ArrayList<>());
        teleportedEntities.add(teleportedEntityInfo);
    }

    @Override
    public void worldthreader$finishReceivingTeleportedEntities() {
        Reference2ReferenceLinkedOpenHashMap<Thread, ServerLevel> worldThreads = Objects.requireNonNull(((MinecraftServerExtended) this.getServer()).worldthreader$getThreadingManager()).getWorldThreads();
        for (ServerLevel source : worldThreads.values()) {
            ArrayList<TeleportedEntityInfo> teleportedEntityList = this.receivedEntities.remove(source);
            if (teleportedEntityList != null) {
                for (TeleportedEntityInfo teleportedEntity : teleportedEntityList) {
                    DimensionChangeHelper.nonPassengerArriveInWorld(teleportedEntity, teleportedEntity.oldEntityObject(), (ServerLevel) (Object) this, (ServerLevel) teleportedEntity.oldEntityObject().level());
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
    public TeleportedEntityInfo worldthreader$getCurrentlyArrivingEntityInfo() {
        return this.currentlyArrivingEntity;
    }

    @Override
    public void worldthreader$setCurrentlyArrivingEntityInfo(TeleportedEntityInfo teleportedEntityInfo) {
        this.currentlyArrivingEntity = teleportedEntityInfo;
    }
}
