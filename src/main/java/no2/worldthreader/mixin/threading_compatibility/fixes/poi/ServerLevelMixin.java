package no2.worldthreader.mixin.threading_compatibility.fixes.poi;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {


    //Fix for issue: Two entities using the same unlinked nether portal at the same time generates multiple nether portals in the other dimension #26
    //Cause of the issue: POIs are sent to be added on the minecraft server, not the world thread.
    //The fix: Run the POI removal / addition on the world thread. No indirection through the event loop as it requires more mixins and isn't needed, because the executor just immediately calls the runnable too.
    @WrapOperation(
            method = {"method_66017", "method_66019"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;execute(Ljava/lang/Runnable;)V")
    )
    private void useWorldThread(MinecraftServer instance, Runnable runnable, Operation<Void> original) {
        if (WorldThreadingManager.hasToAcquireExclusiveAccessBeforeAccessing((ServerLevel) (Object) this)) {
            original.call(instance, runnable);
        } else {
            runnable.run();
        }
    }
}
