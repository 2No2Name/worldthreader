package no2.worldthreader.mixin.dimension_change.recover_failure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements EntityExtended {

    @Shadow
    @Final
    protected EntityEquipment equipment;

    @Shadow
    @Final
    private static Logger LOGGER;

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    public void worldthreader$restoreEntity(TeleportedEntityInfo teleportedEntity) {
        if (this.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION) {
            //Restore like in EntityMixin, cannot call super mixin method
            CompoundTag nbt = Objects.requireNonNull(teleportedEntity.nbtCompound());
            this.unsetRemoved();

            if (nbt.contains(Mob.LEASH_TAG) && this instanceof Leashable leashable) {
                try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
                    ValueInput valueInput = TagValueInput.create(scopedCollector, this.level().registryAccess(), nbt);
                    leashable.readLeashData(valueInput);
                }
            }

            this.worldthreader$restoreEquipment(nbt);

            ((ServerLevel) this.level()).addDuringTeleport(this);
        }
    }

    @Override
    public void worldthreader$restoreEquipment(CompoundTag nbt) {
        RegistryOps<Tag> registryOps = this.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        this.equipment.setAll(nbt.read("equipment", EntityEquipment.CODEC, registryOps).orElseGet(EntityEquipment::new));
    }
}
