package no2.worldthreader.mixin.threading_compatibility;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Shadow @Final private Set<ThrownEnderpearl> enderPearls;

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "getEnderPearls", at = @At("HEAD"))
    private void serializeExecution(CallbackInfoReturnable<Set<ThrownEnderpearl>> cir) {
        if (this.getServer() != null && ((MinecraftServerExtended) this.getServer()).worldthreader$isTickMultithreaded()) {
            for (var pearl : this.enderPearls) {
                if (!WorldThreadingManager.isThreadOwningWorld((ServerLevel) pearl.level())) {
                    //Fallback to serial execution
                    this.getServer().getAllLevels();
                    return;
                }
            }
        }
    }
}
