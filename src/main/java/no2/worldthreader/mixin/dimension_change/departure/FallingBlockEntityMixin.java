package no2.worldthreader.mixin.dimension_change.departure;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin implements EntityExtended {

    @Shadow public boolean forceTickAfterTeleportToDuplicate;

    @Override
    public void worldthreader$onEntityDepartsFromServerWorld(ResourceKey<Level> targetDimension, ResourceKey<Level> sourceDimension) {
        //noinspection UnnecessaryLocalVariable
        boolean entersOrLeavesEnd = (sourceDimension == Level.END || targetDimension == Level.END) && sourceDimension != targetDimension;
        this.forceTickAfterTeleportToDuplicate = entersOrLeavesEnd;
    }
}
