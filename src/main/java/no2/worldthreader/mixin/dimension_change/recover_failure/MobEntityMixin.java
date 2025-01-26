package no2.worldthreader.mixin.dimension_change.recover_failure;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends Entity implements EntityExtended {
    @Shadow
    @Final
    private NonNullList<ItemStack> armorItems;
    @Shadow
    @Final
    private NonNullList<ItemStack> handItems;

    @Shadow private ItemStack bodyArmorItem;

    public MobEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    public void worldthreader$restoreEntity(TeleportedEntityInfo teleportedEntity) {
        if (this.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION) {
            //Restore like in EntityMixin, cannot call super mixin method
            CompoundTag nbt = Objects.requireNonNull(teleportedEntity.nbtCompound());
            this.unsetRemoved();

            if (nbt.contains(Mob.LEASH_TAG, Tag.TAG_COMPOUND) && this instanceof Leashable leashable) {
                leashable.readLeashData(nbt);
            }

            this.worldthreader$restoreEquipment(nbt);

            ((ServerLevel) this.level()).addDuringTeleport(this);
        }
    }

    @Override
    public void worldthreader$restoreEquipment(CompoundTag nbt) {
        //For MobEntities this includes Armor/Hand items
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
        if (nbt.contains("body_armor_item", 10)) {
            this.bodyArmorItem = ItemStack.parseOptional(this.registryAccess(), nbt.getCompound("body_armor_item"));
        }
    }
}
