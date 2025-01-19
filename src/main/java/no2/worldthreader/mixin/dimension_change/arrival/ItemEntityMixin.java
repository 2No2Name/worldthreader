package no2.worldthreader.mixin.dimension_change.arrival;

import net.minecraft.resources.ResourceKey;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements EntityExtended {

    @Shadow protected abstract void mergeWithNeighbours();

    @Override
    public void worldthreader$onArrivedInServerWorld(ResourceKey<Level> targetDimension, ResourceKey<Level> sourceDimension) {
        this.mergeWithNeighbours();
    }
}
