package no2.worldthreader.mixin.dimension_change.arrival;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PrimedTnt.class)
public abstract class PrimedTntMixin implements EntityExtended {

    @Shadow protected abstract void setUsedPortal(boolean bl);

    @Override
    public void worldthreader$onArrivedInServerWorld(ResourceKey<Level> targetDimension, ResourceKey<Level> sourceDimension) {
        this.setUsedPortal(true);
    }
}
