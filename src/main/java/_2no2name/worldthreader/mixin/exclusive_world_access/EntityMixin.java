package _2no2name.worldthreader.mixin.exclusive_world_access;

import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(Entity.class)
public class EntityMixin {
    @Redirect(
            method = "tickNetherPortal()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/util/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;")
    )
    private ServerWorld getWorldWithoutExclusiveAccess(MinecraftServer server, RegistryKey<World> key) {
        return ((MinecraftServerExtended) server).getWorldUnsynchronized(key);
    }

    @Redirect(
            method = "getScoreboardTeam",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getScoreboard()Lnet/minecraft/scoreboard/Scoreboard;")
    )
    private Scoreboard getScoreboardWithoutExclusiveAccess(World world) {
        if (world instanceof ServerWorld) {
            return ((MinecraftServerExtended) Objects.requireNonNull(world.getServer())).getScoreboardUnsynchronized();
        }
        return world.getScoreboard();
    }
}
