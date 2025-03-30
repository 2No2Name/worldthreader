package no2.worldthreader.mixin.threading_compatibility;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Sensor.class)
public class SensorMixin {

    @Shadow
    @Final
    private static TargetingConditions TARGET_CONDITIONS;
    @Shadow
    @Final
    private static TargetingConditions TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING;
    @Shadow
    @Final
    private static TargetingConditions ATTACK_TARGET_CONDITIONS;
    @Shadow
    @Final
    private static TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING;
    @Shadow
    @Final
    private static TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT;
    @Shadow
    @Final
    private static TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT;

    @Unique
    private static final ThreadLocal<TargetingConditions> TL_TARGET_CONDITIONS;
    @Unique
    private static final ThreadLocal<TargetingConditions> TL_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING;
    @Unique
    private static final ThreadLocal<TargetingConditions> TL_ATTACK_TARGET_CONDITIONS;
    @Unique
    private static final ThreadLocal<TargetingConditions> TL_ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING;
    @Unique
    private static final ThreadLocal<TargetingConditions> TL_ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT;
    @Unique
    private static final ThreadLocal<TargetingConditions> TL_ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT;

    static {
        TL_TARGET_CONDITIONS = ThreadLocal.withInitial(() -> TARGET_CONDITIONS.copy());
        TL_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = ThreadLocal.withInitial(() -> TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.copy());
        TL_ATTACK_TARGET_CONDITIONS = ThreadLocal.withInitial(() -> ATTACK_TARGET_CONDITIONS.copy());
        TL_ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = ThreadLocal.withInitial(() -> ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.copy());
        TL_ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT = ThreadLocal.withInitial(() -> ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.copy());
        TL_ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT = ThreadLocal.withInitial(() -> ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.copy());
    }

    @ModifyReceiver(
            method = "updateTargetingConditionRanges",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/sensing/Sensor;TARGET_CONDITIONS:Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;")
    )
    private TargetingConditions updateThreadLocalTargetConditions(TargetingConditions receiver) {
        return TL_TARGET_CONDITIONS.get();
    }

    @ModifyReceiver(
            method = "updateTargetingConditionRanges",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/sensing/Sensor;TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING:Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;")
    )
    private TargetingConditions updateThreadLocalTargetConditionsIgnoreInvisibilityTesting(TargetingConditions receiver) {
        return TL_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.get();
    }

    @ModifyReceiver(
            method = "updateTargetingConditionRanges",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/sensing/Sensor;ATTACK_TARGET_CONDITIONS:Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;")
    )
    private TargetingConditions updateThreadLocalAttackTargetConditions(TargetingConditions receiver) {
        return TL_ATTACK_TARGET_CONDITIONS.get();
    }

    @ModifyReceiver(
            method = "updateTargetingConditionRanges",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/sensing/Sensor;ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING:Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;")
    )
    private TargetingConditions updateThreadLocalAttackTargetConditionsIgnoreInvisibilityTesting(TargetingConditions receiver) {
        return TL_ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.get();
    }

    @ModifyReceiver(
            method = "updateTargetingConditionRanges",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/sensing/Sensor;ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT:Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;")
    )
    private TargetingConditions updateThreadLocalAttackTargetConditionsIgnoreLineOfSight(TargetingConditions receiver) {
        return TL_ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.get();
    }

    @ModifyReceiver(
            method = "updateTargetingConditionRanges",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/sensing/Sensor;ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT:Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;")
    )
    private TargetingConditions updateThreadLocalAttackTargetConditionsIgnoreInvisibilityAndLineOfSight(TargetingConditions receiver) {
        return TL_ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.get();
    }
}
