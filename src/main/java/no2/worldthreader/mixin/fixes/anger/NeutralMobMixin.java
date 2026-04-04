package no2.worldthreader.mixin.fixes.anger;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.EntityReferenceExtended;
import no2.worldthreader.common.thread.WorldThreadingManager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.UUID;

@Mixin(NeutralMob.class)
public interface NeutralMobMixin {

    @WrapOperation(
            method = "updatePersistentAnger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityReference;getLivingEntity(Lnet/minecraft/world/entity/EntityReference;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/LivingEntity;")
    )
    private @Nullable LivingEntity avoidOtherDimensionAccess(@Nullable EntityReference<LivingEntity> reference, Level level, Operation<LivingEntity> original, @Share("playerGameMode") LocalRef<GameType> gameModeRef) {
        if (reference != null && level instanceof ServerLevel serverLevel) {
            WorldThreadingManager worldThreadingManager = WorldThreadingManager.get(serverLevel);
            if (worldThreadingManager.isMultiThreadedPhase()) {
                UUID uuid = reference.getUUID();
                if (!worldThreadingManager.wasUUIDAPlayer(uuid)) {
                    return null; //instanceof Player will return false
                }

                //noinspection unchecked
                if (!((EntityReferenceExtended<LivingEntity>) (Object) reference).worldthreader$isEntityInSameDimension(level, LivingEntity.class)) {
                    //Avoid interdimensional entity access, since this requires exclusive world access (slow!)
                    GameType lastPlayerGameMode = worldThreadingManager.getLastPlayerGameMode(uuid);
                    gameModeRef.set(lastPlayerGameMode);
                    return null; // instanceof Player mixin will look at the gameModeRef and return true
                }
                //Use vanilla code when entity is in same dimension, for maximum mod compatibility
            }
            //Use vanilla code when not multithreaded
        }
        return original.call(reference, level);
    }

    @Definition(id = "persistentAngerTarget", local = @Local(type = LivingEntity.class, name = "persistentTarget"))
    @Expression("persistentAngerTarget instanceof ?")
    @WrapOperation(
            method = "updatePersistentAnger", at = @At("MIXINEXTRAS:EXPRESSION"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityReference;getLivingEntity(Lnet/minecraft/world/entity/EntityReference;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/LivingEntity;")
            )
    )
    private boolean isTargetCreativeOrSpectatorPlayerOrIsPeaceful(Object object, Operation<Boolean> original, @Share("playerGameMode") LocalRef<GameType> gameModeRef) {
        if (gameModeRef.get() != null) {
            return true; //player wasn't null, just in another dimension, see mixin above
        }
        return original.call(object);
    }

    @WrapOperation(
            method = "updatePersistentAnger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isCreative()Z")
    )
    private boolean isCreative(Player instance, Operation<Boolean> original, @Share("playerGameMode") LocalRef<GameType> gameModeRef) {
        if (gameModeRef.get() == GameType.CREATIVE) {
            return true;
        }
        return original.call(instance);
    }

    @WrapOperation(
            method = "updatePersistentAnger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z")

    )
    private boolean isSpectator(Player instance, Operation<Boolean> original, @Share("playerGameMode") LocalRef<GameType> gameModeRef) {
        if (gameModeRef.get() == GameType.SPECTATOR) {
            return true;
        }
        return original.call(instance);
    }
}
