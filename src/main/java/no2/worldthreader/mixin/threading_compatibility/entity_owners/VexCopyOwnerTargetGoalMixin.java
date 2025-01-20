package no2.worldthreader.mixin.threading_compatibility.entity_owners;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Vex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/world/entity/monster/Vex$VexCopyOwnerTargetGoal")
public class VexCopyOwnerTargetGoalMixin {

    @Redirect(
            method = {"canUse()Z", "start()V"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/monster/Vex;owner:Lnet/minecraft/world/entity/Mob;")
    )
    private Mob getOwnerWithGetterFunction(Vex instance) {
        return instance.getOwner();
    }
}
