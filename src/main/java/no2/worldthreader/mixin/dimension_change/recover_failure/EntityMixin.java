package no2.worldthreader.mixin.dimension_change.recover_failure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtended {
    @Shadow public abstract @Nullable Entity.RemovalReason getRemovalReason();

    @Shadow protected abstract void unsetRemoved();

	@Shadow
	public abstract Level level();

	@Shadow
	public abstract ProblemReporter.PathElement problemPath();

	@Shadow
	@Final
	private static Logger LOGGER;

	@Override
	public void worldthreader$restoreEntity(TeleportedEntityInfo teleportedEntity) {
		if (this.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION) {
			this.unsetRemoved();

			CompoundTag nbt = Objects.requireNonNull(teleportedEntity.nbtCompound());
			if (nbt.contains(Mob.LEASH_TAG) && this instanceof Leashable leashable) {
				try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
					ValueInput valueInput = TagValueInput.create(scopedCollector, this.level().registryAccess(), nbt);
					leashable.readLeashData(valueInput);
				}
			}

			((ServerLevel) this.level()).addDuringTeleport((Entity) (Object) this);
		}
	}
}
