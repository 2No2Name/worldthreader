package no2.worldthreader.mixin.dimension_change.recover_failure;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin extends Entity implements EntityExtended {


    @Shadow @Final private NonNullList<ItemStack> armorItems;

    @Shadow @Final private NonNullList<ItemStack> handItems;

    public ArmorStandMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void worldthreader$restoreEquipment(CompoundTag nbt) {
        //For MobEntities this includes Armor/Hand items // ArmorStand has different armorItems and handItems fields!
        if (nbt.contains("ArmorItems", Tag.TAG_LIST)) {
            ListTag armorItems = nbt.getList("ArmorItems", Tag.TAG_COMPOUND);
            for (int i = 0; i < this.armorItems.size(); ++i) {
                this.armorItems.set(i, ItemStack.parseOptional(this.registryAccess(), armorItems.getCompound(i)));
            }
        }
        if (nbt.contains("HandItems", Tag.TAG_LIST)) {
            ListTag handItems = nbt.getList("HandItems", Tag.TAG_COMPOUND);
            for (int i = 0; i < this.handItems.size(); ++i) {
                this.handItems.set(i, ItemStack.parseOptional(this.registryAccess(), handItems.getCompound(i)));
            }
        }
    }
}
