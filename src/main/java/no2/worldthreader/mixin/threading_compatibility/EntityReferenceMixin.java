package no2.worldthreader.mixin.threading_compatibility;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import no2.worldthreader.common.mixin_support.interfaces.EntityReferenceExtended;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(EntityReference.class)
public abstract class EntityReferenceMixin<StoredEntityType extends UniquelyIdentifyable> implements EntityReferenceExtended<StoredEntityType> {

    @Shadow
    private Either<UUID, StoredEntityType> entity;

    @Shadow
    @Nullable
    protected abstract StoredEntityType resolve(@Nullable UniquelyIdentifyable entity, Class<StoredEntityType> clazz);

    @Inject(
            method = "getEntity(Lnet/minecraft/world/level/Level;Ljava/lang/Class;)Lnet/minecraft/world/level/entity/UniquelyIdentifyable;",
            at = @At("HEAD")
    )
    private void checkCrossDimensionAccess(Level level, Class<StoredEntityType> class_, CallbackInfoReturnable<StoredEntityType> cir) {
        Optional<StoredEntityType> right = this.entity.right();
        if (level instanceof ServerLevel serverLevel && right.isPresent() && right.get() instanceof Entity e && e.level() != level) {
            serverLevel.getServer().getAllLevels();
        }
    }

    @Override
    public boolean worldthreader$isEntityInSameDimension(Level level, Class<StoredEntityType> storedEntityType) {
        return this.worldthreader$getEntitySameDimension(level, storedEntityType) != null;
    }

    @Override
    public StoredEntityType worldthreader$getEntitySameDimension(Level level, Class<StoredEntityType> clazz) {
        Optional<StoredEntityType> stored = this.entity.right();
        if (stored.isPresent()) {
            StoredEntityType storedEntity = stored.get();

            //Only get entities in the same dimension
            if (storedEntity instanceof Entity e && e.level() != level) {
                return null;
            }

            if (!storedEntity.isRemoved()) {
                return storedEntity;
            }

            this.entity = Either.left(storedEntity.getUUID());
        }

        Optional<UUID> uuid = this.entity.left();
        if (uuid.isPresent()) {
            //Only get entities in the same dimension
            StoredEntityType resolved = this.resolve(level.getEntity(uuid.get()), clazz);
            if (resolved != null && !resolved.isRemoved()) {
                this.entity = Either.right(resolved);
                return resolved;
            }
        }

        return null;
    }
}
