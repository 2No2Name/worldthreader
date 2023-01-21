package _2no2name.worldthreader.mixin.exclusive_world_access;

import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import _2no2name.worldthreader.common.thread.WorldThreadingManager;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Objects;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerExtended {

    @Shadow
    @Final
    private Map<RegistryKey<World>, ServerWorld> worlds;

    @Override
    public ServerWorld getWorldUnsynchronized(RegistryKey<World> key) {
        return this.worlds.get(key);
    }

    private void acquireSingleThreadedWorldAccess() {
        if (this.isTickMultithreaded()) {
            WorldThreadingManager worldThreadingManager = Objects.requireNonNull(this.getWorldThreadingManager());
            worldThreadingManager.waitForExclusiveWorldAccess();
        }
    }

    @Inject(
            method = "getWorlds()Ljava/lang/Iterable;",
            at = @At("HEAD")
    )
    private void avoidParallelWorldAccess1(CallbackInfoReturnable<Iterable<ServerWorld>> cir) {
        this.acquireSingleThreadedWorldAccess();
    }

    @Inject(
            method = "getWorld(Lnet/minecraft/util/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;",
            at = @At("HEAD")
    )
    private void avoidParallelWorldAccess2(RegistryKey<World> key, CallbackInfoReturnable<@Nullable ServerWorld> cir) {
        if (this.isTickMultithreaded()) {
            WorldThreadingManager worldThreadingManager = Objects.requireNonNull(this.getWorldThreadingManager());
            Reference2ReferenceLinkedOpenHashMap<Thread, ServerWorld> worldThreads = worldThreadingManager.getWorldThreads();
            ServerWorld serverWorld = worldThreads.get(Thread.currentThread());
            if (serverWorld == null) {
                //Whatever is happening here, it is an offthread access that this mod did not cause.
                return;
            }
            //If the thread is accessing its own world, that is fine. Otherwise acquire exclusive access
            if (!key.equals(serverWorld.getRegistryKey())) {
                this.acquireSingleThreadedWorldAccess();
            }
        }
    }

    @Inject(
            method = "getOverworld()Lnet/minecraft/server/world/ServerWorld;",
            at = @At("HEAD")
    )
    private void avoidParallelWorldAccess3(CallbackInfoReturnable<ServerWorld> cir) {
        this.acquireSingleThreadedWorldAccess();
    }

    @Inject(
            method = {"getDataPackManager", "getScoreboard", "getDataCommandStorage", "getBossBarManager", "getCommandFunctionManager"},
            at = @At("HEAD")
    )
    private void avoidParallelServerDataAccess(CallbackInfoReturnable<ServerWorld> cir) {
        this.acquireSingleThreadedWorldAccess();
    }
}
