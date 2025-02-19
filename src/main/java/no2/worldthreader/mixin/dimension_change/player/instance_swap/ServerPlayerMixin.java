package no2.worldthreader.mixin.dimension_change.player.instance_swap;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import no2.worldthreader.common.mixin_support.interfaces.ServerPlayerInstanceSwapper;
import no2.worldthreader.common.thread.ThreadLocals;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayer.class)
public abstract class ServerPlayerMixin {


    @Shadow public ServerGamePacketListenerImpl connection;

    @Shadow @Final public MinecraftServer server;

    @Shadow public abstract ServerLevel serverLevel();

    // Allow null level to be passed in ctor:
    @WrapOperation(
            method = "<init>", require = 2,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;")
    )
    private static BlockPos handleNullWorld(ServerLevel serverLevel, Operation<BlockPos> original) {
        if (ThreadLocals.PLAYER_SWAP_4_LEVEL.get() == serverLevel) {
            return BlockPos.ZERO;
        }
        return original.call(serverLevel);
    }
    // Allow null level to be passed in ctor:
    @WrapOperation(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnAngle()F")
    )
    private static float handleNullWorld1(ServerLevel serverLevel, Operation<Float> original) {
        if (ThreadLocals.PLAYER_SWAP_4_LEVEL.get() == serverLevel) {
            return 0.0F;
        }
        return original.call(serverLevel);
    }
    // Allow null level to be passed in ctor:
    @WrapOperation(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;adjustSpawnLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;")
    )
    private BlockPos handleNullWorld2(ServerPlayer instance, ServerLevel serverLevel, BlockPos blockPos, Operation<BlockPos> original) {
        if (ThreadLocals.PLAYER_SWAP_4_LEVEL.get() == serverLevel) {
            return blockPos;
        }
        return original.call(instance, serverLevel, blockPos);
    }

    @Redirect(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;", ordinal = 1)
    )
    private ResourceKey<Level> getDimensionOrRecover(ServerLevel instance) {
        if (WorldThreadingManager.isRecoveringTeleports(instance)) {
            return null;
        }
        return instance.dimension();
    }

    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V")
    )
    private void swapPlayerWithNewInstance(TeleportTransition teleportTransition, CallbackInfoReturnable<ServerPlayer> cir,
                                           @Local(ordinal = 0) ServerLevel newLevel, @Local(ordinal = 1) ServerLevel oldLevel, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        if (newLevel == oldLevel) {
            if (WorldThreadingManager.isRecoveringTeleports(newLevel)) {
                //Undo the effect of all the mixins which redirect to the new player instance.
                // The `this` instance is teleported back to where it came from
                newPlayerRef.set((ServerPlayer) (Object) this);
                return;
            }

            throw new AssertionError("Worldthreader: Mixin placed at incorrect position in ServerPlayer.teleport!");
        }
        if (newLevel == this.serverLevel()) {
            throw new AssertionError("Worldthreader: Ordinals of Local Capture are incorrect!");
        }

        if (WorldThreadingManager.isWrongThreadForWorld(newLevel)) {
            throw new IllegalStateException("Worldthreader: Must create new player on destination world thread!");
        }

        //Swap the player with a new instance, what could go wrong?
        ServerPlayer serverPlayer = ((ServerPlayerInstanceSwapper) this.connection).worldthreader$swapRemovedPlayerWithNewCopy((ServerPlayer) (Object) this, newLevel);
        newPlayerRef.set(serverPlayer);
    }

    //Use the new player instead of this for all consecutive calls
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
    @ModifyReturnValue(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "RETURN"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    )
    private ServerPlayer useNewPlayer16(ServerPlayer original, @Share("NewPlayer") LocalRef<ServerPlayer> newPlayerRef) {
        return newPlayerRef.get();
    }
}
