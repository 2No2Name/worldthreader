package no2.worldthreader.mixin.dimension_change.arrival;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin extends Entity implements EntityExtended {

    public ThrownEnderpearlMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void worldthreader$onArrivedInServerWorld(ResourceKey<Level> targetDimension, ResourceKey<Level> sourceDimension) {
        this.placePortalTicket(BlockPos.containing(this.position()));
    }
}
