package no2.worldthreader.mixin.dimension_change.departure.triggers.ender_pearl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import no2.worldthreader.mixin.threading_compatibility.entity_owners.ProjectileMixin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;
import java.util.UUID;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin extends ProjectileMixin {

    @Unique
    private boolean hasServerPlayerAsOwner;


    @Shadow
    public abstract @Nullable Entity getOwner();

    public ThrownEnderpearlMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void setCachedOwnerWrapped(Projectile theEnderPearl, @Nullable EntityReference<Entity> entityReference) {
        EntityReference<Entity> previousOwner = this.worldthreader$getCachedOwnerUnsafe();
        super.setCachedOwnerWrapped(theEnderPearl, entityReference);
        if (previousOwner == entityReference) {
            return;
        }
        Entity newOwner = this.getOwner();
        boolean isPlayer = newOwner instanceof ServerPlayer;
        this.hasServerPlayerAsOwner = isPlayer;
        if (isPlayer) {
            this.ensureThreadsafeAccess(newOwner);
            //Register the enderpearl more reliably than vanilla. Then omit redundant registering during the enderpearl tick which would require exclusive world access
            ((ServerPlayer) newOwner).registerEnderPearl((ThrownEnderpearl) (Object) this);
        }
    }

    @Unique
    private void ensureThreadsafeAccess(Entity cachedOwner) {
        if (WorldThreadingManager.hasToAcquireExclusiveAccessBeforeAccessing((ServerLevel) cachedOwner.level())) {
            Objects.requireNonNull(this.level().getServer()).getAllLevels();
        }
    }


    @Redirect(
            method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownEnderpearl;findOwnerIncludingDeadPlayer(Lnet/minecraft/server/level/ServerLevel;Ljava/util/UUID;)Lnet/minecraft/world/entity/Entity;")
    )
    private Entity avoidGettingOwner(ServerLevel serverLevel, UUID uUID) {
        if (this.level() instanceof ServerLevel level && ((MinecraftServerExtended) level.getServer()).worldthreader$isTickMultithreaded()) {
            return null;
        }
        return this.getOwner();
    }

    @WrapOperation(
            method = "tick",
            constant = {
                    @Constant(classValue = ServerPlayer.class, ordinal = 0), //Ordinal 1 targets CHECKCAST for some reason
                    @Constant(classValue = ServerPlayer.class, ordinal = 2)
            }
    )
    private boolean handleNullPlayer(Object object, Operation<Boolean> original) {
        if (object == null) {
            return this.hasServerPlayerAsOwner;
        }
        return original.call(object);
    }

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isAlive()Z")
    )
    private boolean handleNullPlayer2(Entity instance, Operation<Boolean> original) {
        if (instance == null && this.hasServerPlayerAsOwner && this.owner != null) {
            WorldThreadingManager worldThreadingManager = WorldThreadingManager.get((ServerLevel) this.level());
            if (worldThreadingManager != null && worldThreadingManager.isMultiThreadedPhase()) {
                return worldThreadingManager.wasAlive(this.owner.getUUID());
            }
        }
        return original.call(instance);
    }

    @WrapOperation(
            method = "tick",
            at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;wonGame:Z")
    )
    private boolean handleNullPlayer2(ServerPlayer instance, Operation<Boolean> original) {
        if (instance == null && this.hasServerPlayerAsOwner && this.owner != null) {
            WorldThreadingManager worldThreadingManager = WorldThreadingManager.get((ServerLevel) this.level());
            if (worldThreadingManager != null && worldThreadingManager.isMultiThreadedPhase()) {
                return worldThreadingManager.wonGame(this.owner.getUUID());
            }
        }
        return original.call(instance);
    }


    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;level()Lnet/minecraft/server/level/ServerLevel;")
    )
    private ServerLevel getServerLevel(ServerPlayer instance, Operation<ServerLevel> original) {
        if (instance == null) {
            return (ServerLevel) this.level();
        }
        return original.call(instance);
    }

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;registerAndUpdateEnderPearlTicket(Lnet/minecraft/world/entity/projectile/ThrownEnderpearl;)J")
    )
    private long registerAndUpdate(ServerPlayer instance, ThrownEnderpearl thrownEnderpearl, Operation<Long> original) {
        if (instance == null) {
            if (thrownEnderpearl.level() instanceof ServerLevel serverLevel) {
                serverLevel.resetEmptyTime();
                return ServerPlayer.placeEnderPearlTicket(serverLevel, thrownEnderpearl.chunkPosition()) - 1L;
            } else {
                return 0L;
            }
        }
        return original.call(instance, thrownEnderpearl);
    }
}
