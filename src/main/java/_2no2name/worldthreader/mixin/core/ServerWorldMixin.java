package _2no2name.worldthreader.mixin.core;


import _2no2name.worldthreader.common.ServerWorldTicking;
import _2no2name.worldthreader.common.dimension_change.TeleportedEntityInfo;
import _2no2name.worldthreader.common.mixin_support.interfaces.EntityExtended;
import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import _2no2name.worldthreader.common.mixin_support.interfaces.ServerWorldExtended;
import _2no2name.worldthreader.common.thread.WorldThreadingManager;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements ServerWorldExtended {

    private final Map<ServerWorld, ArrayList<TeleportedEntityInfo>> receivedEntities = new Reference2ReferenceOpenHashMap<>();
    private TeleportedEntityInfo currentlyArrivingEntity;

    @Shadow
    protected abstract void tickWeather();

    @Shadow
    @NotNull
    public abstract MinecraftServer getServer();

    @Override
    public void receiveTeleportedEntity(Entity staleEntityObject, NbtCompound entityNBT, ServerWorld source, Direction.Axis portalAxis, Vec3d inPortalPos) {
        ArrayList<TeleportedEntityInfo> teleportedEntities = this.receivedEntities.computeIfAbsent(source, (ServerWorld s) -> new ArrayList<>());
        teleportedEntities.add(new TeleportedEntityInfo(staleEntityObject, entityNBT, portalAxis, inPortalPos));
    }

    @Override
    public void finishReceivingTeleportedEntities() {
        Reference2ReferenceLinkedOpenHashMap<Thread, ServerWorld> worldThreads = Objects.requireNonNull(((MinecraftServerExtended) this.getServer()).getWorldThreadingManager()).getWorldThreads();
        for (ServerWorld source : worldThreads.values()) {
            ArrayList<TeleportedEntityInfo> teleportedEntityList = this.receivedEntities.remove(source);
            if (teleportedEntityList != null) {
                for (TeleportedEntityInfo teleportedEntity : teleportedEntityList) {
                    this.currentlyArrivingEntity = teleportedEntity;
                    ((EntityExtended) teleportedEntity.staleEntityObject()).arriveInWorld(teleportedEntity.nbtCompound(), (ServerWorld) (Object) this);
                    this.currentlyArrivingEntity = null;
                }
            }
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickWeather()V"), method = "tick")
    private void tickWeatherThreaded(ServerWorld instance) {
        if (((MinecraftServerExtended) this.getServer()).isTickMultithreaded()) {
            this.tickWeatherThreaded();
        } else {
            this.tickWeather();
        }
    }

    private void tickWeatherThreaded() {
        WorldThreadingManager worldThreadingManager = Objects.requireNonNull(((MinecraftServerExtended) ((ServerWorld) (Object) this).getServer()).getWorldThreadingManager());
        boolean isMainWorld = ServerWorldTicking.isMainWorld((ServerWorld) (Object) this);
        if (!isMainWorld) {
            //Dependent worlds need to wait for the main world to update the weather first, otherwise they might update their weather based on
            //outdated values or values read with race conditions.
            worldThreadingManager.withinTickBarrier();
            this.tickWeather();
        } else {
            //Update the weather from the main world immediately
            this.tickWeather();
            //The main world does not need to wait for dependent worlds ticking their weather
            worldThreadingManager.withinTickBarrier();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;calculateAmbientDarkness()V"), method = "tick")
    private void calcAmbientDarknessBeforeTickingTime(ServerWorld serverWorld) {
        if (((MinecraftServerExtended) this.getServer()).isTickMultithreaded()) {
            //Only calculate ambient darkness on the main world here, delay for other worlds
            boolean isMainWorld = ServerWorldTicking.isMainWorld((ServerWorld) (Object) this);
            if (isMainWorld) {
                //Update the weather from the main world immediately
                serverWorld.calculateAmbientDarkness();
            }
        } else {
            serverWorld.calculateAmbientDarkness();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickTime()V", shift = At.Shift.AFTER), method = "tick")
    private void calcAmbientDarknessAfterTickingTime(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        MinecraftServerExtended server = (MinecraftServerExtended) this.getServer();
        if (server.isTickMultithreaded()) {
            WorldThreadingManager worldThreadingManager = Objects.requireNonNull(server.getWorldThreadingManager());
            boolean isMainWorld = ServerWorldTicking.isMainWorld((ServerWorld) (Object) this);

            //The main world does not need to wait for dependent worlds ticking their weather or ambient darkness
            //Dependent worlds need to wait for the main world to update the time first, otherwise they might update their ambient darkness based on
            //outdated values or values read with race conditions.
            worldThreadingManager.withinTickBarrier();
            if (!isMainWorld) {
                ((ServerWorld) (Object) this).calculateAmbientDarkness();
            }
        }
    }

    @Override
    public TeleportedEntityInfo getCurrentlyArrivingEntityInfo() {
        return this.currentlyArrivingEntity;
    }
}
