package no2.worldthreader.mixin.threading_compatibility;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(EntityReference.class)
public class EntityReferenceMixin<StoredEntityType extends UniquelyIdentifyable> {

    @Shadow
    private Either<UUID, StoredEntityType> entity;

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
}
