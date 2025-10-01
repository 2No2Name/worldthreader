package no2.worldthreader.mixin.exclusive_world_access;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Objects;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerExtended {

    @Shadow @Final private Map<ResourceKey<Level>, ServerLevel> levels;

    @Override
    public ServerLevel worldthreader$getLevelUnsynchronized(ResourceKey<Level> key) {
        return this.levels.get(key);
    }

    @Unique
    private void acquireSingleThreadedWorldAccess() {
        if (this.worldthreader$isTickMultithreaded()) {
            WorldThreadingManager worldThreadingManager = Objects.requireNonNull(this.worldthreader$getThreadingManager());
            worldThreadingManager.waitForExclusiveWorldAccess(false);
        }
    }

    @Inject(
            method = "getAllLevels",
            at = @At("HEAD")
    )
    private void avoidParallelWorldAccess1(CallbackInfoReturnable<Iterable<ServerLevel>> cir) {
        this.acquireSingleThreadedWorldAccess();
    }

    @Inject(
            method = "getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;",
            at = @At("HEAD")
    )
    private void avoidParallelWorldAccess2(ResourceKey<Level> key, CallbackInfoReturnable<@Nullable ServerLevel> cir) {
        if (this.worldthreader$isTickMultithreaded()) {
            WorldThreadingManager worldThreadingManager = Objects.requireNonNull(this.worldthreader$getThreadingManager());
            if (!worldThreadingManager.isWorldThread(Thread.currentThread())) {
                //Whatever is happening here, it is an offthread access that this mod did not cause.
                // Future: Allow non-world threads to also acquire single threaded world access!
                return;
            }
            //If the thread is accessing its own world, that is fine. Otherwise, acquire exclusive access
            if (!worldThreadingManager.isWorldThreadOf(key)) {
                this.acquireSingleThreadedWorldAccess();
            }
        }
    }

    @Inject(
            method = "overworld()Lnet/minecraft/server/level/ServerLevel;",
            at = @At("HEAD")
    )
    private void avoidParallelWorldAccess3(CallbackInfoReturnable<ServerLevel> cir) {
        this.acquireSingleThreadedWorldAccess();
    }

    @Inject(
            method = {"getPackRepository", "getCommandStorage", "getCustomBossEvents", "getFunctions"},
            at = @At("HEAD")
    )
    private void avoidParallelServerDataAccess(CallbackInfoReturnable<ServerLevel> cir) {
        this.acquireSingleThreadedWorldAccess();
    }
}
