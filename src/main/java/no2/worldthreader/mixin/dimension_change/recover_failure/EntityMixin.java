package no2.worldthreader.mixin.dimension_change.recover_failure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtended {
    @Shadow public abstract @Nullable Entity.RemovalReason getRemovalReason();

    @Shadow protected abstract void unsetRemoved();

	@Shadow
	public abstract Level level();

	@Override
	public void worldthreader$restoreEntity(TeleportedEntityInfo teleportedEntity) {
		if (this.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION) {
			this.unsetRemoved();

			CompoundTag nbt = teleportedEntity.nbtCompound();
			if (nbt.contains(Mob.LEASH_TAG, Tag.TAG_COMPOUND) && this instanceof Leashable leashable) {
				leashable.readLeashData(nbt);
			}

			((ServerLevel) this.level()).addDuringTeleport((Entity) (Object) this);
		}
	}
}
