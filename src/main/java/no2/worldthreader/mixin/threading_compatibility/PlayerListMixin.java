package no2.worldthreader.mixin.threading_compatibility;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserBanList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(
            method = {"op(Lcom/mojang/authlib/GameProfile;)V", "deop(Lcom/mojang/authlib/GameProfile;)V"},
            at = @At(value = "HEAD")
    )
    private void ensureSafety(GameProfile gameProfile, CallbackInfo ci) {
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
