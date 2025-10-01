package no2.worldthreader.mixin.threading_compatibility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserBanList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Inject(
            method = "op(Lnet/minecraft/server/players/NameAndId;Ljava/util/Optional;Ljava/util/Optional;)V",
            at = @At(value = "HEAD")
    )
    private void ensureSafety(NameAndId nameAndId, Optional<Integer> optional, Optional<Boolean> optional2, CallbackInfo ci) {
        MinecraftServer minecraftServer = this.server;
        if (minecraftServer != null) {
            minecraftServer.getAllLevels();
        }
    }

    @Inject(
            method = "deop(Lnet/minecraft/server/players/NameAndId;)V",
            at = @At(value = "HEAD")
    )
    private void ensureSafety(NameAndId nameAndId, CallbackInfo ci) {
        MinecraftServer minecraftServer = this.server;
        if (minecraftServer != null) {
            minecraftServer.getAllLevels();
        }
    }

    @Inject(
            method = {"getBans", "getIpBans"},
            at = @At(value = "HEAD")
    )
    private void ensureSafety(CallbackInfoReturnable<UserBanList> cir) {
        MinecraftServer minecraftServer = this.server;
        if (minecraftServer != null) {
            minecraftServer.getAllLevels();
        }
    }
}
