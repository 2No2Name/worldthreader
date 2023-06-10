package _2no2name.worldthreader.mixin.dimension_change;

import _2no2name.worldthreader.common.mixin_support.interfaces.EntityExtended;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements EntityExtended {

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    private native void tryMerge();

    @Override
    public void onArrivedInWorld() {
        if (!this.getWorld().isClient) {
            this.tryMerge();
        }
    }
}
