package no2.worldthreader.mixin.fixes.last_hurt_by_mob;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    private @Nullable EntityReference<LivingEntity> lastHurtByMob;

    @Shadow
    private int lastHurtByMobTimestamp;

    @Shadow
    public abstract void setLastHurtByMob(@Nullable LivingEntity livingEntity);

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(
            method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getLastHurtByMob()Lnet/minecraft/world/entity/LivingEntity;")
    )
    //Omit the isAlive check here, since it might require interdimensional entity access
    private LivingEntity updateLastHurtByMobAndGetNull(LivingEntity instance) {
        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.lastHurtByMob != null) {
                //noinspection StatementWithEmptyBody
                if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
                    this.setLastHurtByMob(null);
                } else {
                    //TODO check if players need the following special handling:
//                    WorldThreadingManager worldThreadingManager = WorldThreadingManager.get(serverLevel);
//                    if (worldThreadingManager != null && worldThreadingManager.isMultiThreadedPhase()) {
//                        if (!worldThreadingManager.wasPlayerAlive(this.lastHurtByMob.getUUID(), true)) {
//                            this.setLastHurtByMob(null);
//                        }
//                    }

                }
            }
            return null;
        }
        return instance.getLastHurtByMob();

    }

    @ModifyReturnValue(
            method = "getLastHurtByMob", at = @At("RETURN")
    )
    private LivingEntity getAliveLastHurtByMob(LivingEntity original) {
        if (original != null && !original.isAlive()) {
            return null;
        }
        return original;
    }
}
