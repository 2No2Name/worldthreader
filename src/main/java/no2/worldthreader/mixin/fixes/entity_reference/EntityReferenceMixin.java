package no2.worldthreader.mixin.fixes.entity_reference;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.UUIDLookup;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.UUID;

@Mixin(EntityReference.class)
public abstract class EntityReferenceMixin {

    @ModifyArg(
            method = "getEntity(Lnet/minecraft/world/level/Level;Ljava/lang/Class;)Lnet/minecraft/world/level/entity/UniquelyIdentifyable;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityReference;getEntity(Lnet/minecraft/world/level/entity/UUIDLookup;Ljava/lang/Class;)Lnet/minecraft/world/level/entity/UniquelyIdentifyable;",
                    ordinal = 1
            )
    )
    public UUIDLookup<? extends @NotNull UniquelyIdentifyable> getEntity(UUIDLookup<? extends @NotNull UniquelyIdentifyable> uUIDLookup, @Local(argsOnly = true) Level level) {
        if (level instanceof ServerLevel serverLevel) {
            WorldThreadingManager worldThreadingManager = ((MinecraftServerExtended) serverLevel.getServer()).worldthreader$getThreadingManager();
            if (worldThreadingManager != null && worldThreadingManager.isMultiThreadedPhase()) {
                return uUID -> getEntityInAnyDimension(serverLevel, worldThreadingManager, uUID);
            }
        }

        return uUIDLookup;
    }

    /**
     * Avoids accessing the non-threadsafe level entity lookup when the entity is not available.
     * However, this means the data is outdated by up to a tick.
     */
    @Unique
    private Entity getEntityInAnyDimension(ServerLevel serverLevel, WorldThreadingManager worldThreadingManager, UUID uUID) {
        Entity entity = serverLevel.getEntity(uUID);
        if (entity != null) {
            return entity;
        } else {
            ServerLevel levelWithUUID = worldThreadingManager.getUUIDLevel(uUID, serverLevel);
            if (levelWithUUID != null) {
                worldThreadingManager.waitForExclusiveWorldAccess(false);
                return levelWithUUID.getEntity(uUID);// TODO what if the entity changed dimensions in the meantime
            }

            return null;
        }
    }
}
