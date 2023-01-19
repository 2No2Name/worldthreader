package _2no2name.worldthreader.mixin.exclusive_world_access;

import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {
    @Redirect(
            method = "tickNetherPortal()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/util/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;")
    )
    private ServerWorld getWorldWithoutExclusiveAccess(MinecraftServer server, RegistryKey<World> key) {
        return ((MinecraftServerExtended) server).getWorldUnsynchronized(key);
    }
}
