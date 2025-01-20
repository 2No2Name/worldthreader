package no2.worldthreader.mixin.dimension_change.player.instance_swap;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import no2.worldthreader.common.mixin_support.interfaces.TransparentServerPlayerSwapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {


    @Shadow public ServerGamePacketListenerImpl connection;

    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "HEAD")
    )
    private void check(TeleportTransition teleportTransition, CallbackInfoReturnable<ServerPlayer> cir) {
        if (DimensionChangeHelper.isDummy(teleportTransition)) {
            System.out.println("Player teleported with dummy transition!");
        }
    }

    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V")
    )
    private void swapPlayerWithNewInstance(TeleportTransition teleportTransition, CallbackInfoReturnable<ServerPlayer> cir,
                                           @Local(ordinal = 0) ServerLevel newLevel, @Local(ordinal = 1) ServerLevel oldLevel, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        if (newLevel == oldLevel) {
            throw new AssertionError("Worldthreader: Mixin placed at incorrect position in ServerPlayer.teleport!");
        }
        ServerPlayer serverPlayer = ((TransparentServerPlayerSwapper) this.connection).worldthreader$swapRemovedPlayerWithNewCopy((ServerPlayer) (Object) this);
        newPlayerRef.set(serverPlayer);
    }

    @ModifyReceiver(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target= "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V")
    )
    private ServerPlayer useNewPlayer(ServerPlayer instance, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyReceiver(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target= "Lnet/minecraft/server/level/ServerPlayer;position()Lnet/minecraft/world/phys/Vec3;"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer1(ServerPlayer instance, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyReceiver(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "FIELD", target= "Lnet/minecraft/server/level/ServerPlayer;enteredNetherPosition:Lnet/minecraft/world/phys/Vec3;"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer2(ServerPlayer instance, Vec3 value, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyReceiver(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target= "Lnet/minecraft/server/level/ServerPlayer;setServerLevel(Lnet/minecraft/server/level/ServerLevel;)V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer3(ServerPlayer instance, ServerLevel serverLevel, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyReceiver(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "FIELD", target= "Lnet/minecraft/server/level/ServerPlayer;connection:Lnet/minecraft/server/network/ServerGamePacketListenerImpl;"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer4(ServerPlayer instance, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyArg(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target= "Lnet/minecraft/server/level/ServerLevel;addDuringTeleport(Lnet/minecraft/world/entity/Entity;)V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private Entity useNewPlayer5(Entity entity, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyReceiver(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target= "Lnet/minecraft/server/level/ServerPlayer;triggerDimensionChangeTriggers(Lnet/minecraft/server/level/ServerLevel;)V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer6(ServerPlayer instance, ServerLevel serverLevel, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyReceiver(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target= "Lnet/minecraft/server/level/ServerPlayer;stopUsingItem()V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer7(ServerPlayer instance, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyReceiver(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target= "Lnet/minecraft/server/level/ServerPlayer;getAbilities()Lnet/minecraft/world/entity/player/Abilities;"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer8(ServerPlayer instance, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyArg(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target= "Lnet/minecraft/server/players/PlayerList;sendLevelInfo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer9(ServerPlayer serverPlayer, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyArg(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target= "Lnet/minecraft/server/players/PlayerList;sendAllPlayerInfo(Lnet/minecraft/server/level/ServerPlayer;)V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer10(ServerPlayer serverPlayer, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyArg(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target= "Lnet/minecraft/server/players/PlayerList;sendActivePlayerEffects(Lnet/minecraft/server/level/ServerPlayer;)V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer11(ServerPlayer serverPlayer, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyArg(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target= "Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;onTransition(Lnet/minecraft/world/entity/Entity;)V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private Entity useNewPlayer12(Entity entity, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyReceiver(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "FIELD", target= "Lnet/minecraft/server/level/ServerPlayer;lastSentExp:I"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer13(ServerPlayer instance, int value, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyReceiver(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "FIELD", target= "Lnet/minecraft/server/level/ServerPlayer;lastSentHealth:F"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer14(ServerPlayer instance, float value, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
    @ModifyReceiver(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "FIELD", target= "Lnet/minecraft/server/level/ServerPlayer;lastSentFood:I"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer15(ServerPlayer instance, int value, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
}
