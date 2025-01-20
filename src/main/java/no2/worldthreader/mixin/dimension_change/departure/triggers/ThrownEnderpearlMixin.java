package no2.worldthreader.mixin.dimension_change.departure.triggers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.dimension_change.DimensionChangeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin {
    //Todo Replace the ender pearl death, chunk ticket code as it forces serialization many ticks/every tick in plausible scenarios
}
