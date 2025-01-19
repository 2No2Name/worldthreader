package no2.worldthreader.mixin.entity_id_wireless_redstone_fix;

import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldWithWirelessRedstoneFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public abstract Level level();

    @Redirect(
            method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/atomic/AtomicInteger;incrementAndGet()I", remap = false)
    )
    private int getNextEntityIDThreaded(AtomicInteger entityIdCounter) {
        if (this.level() instanceof ServerLevel && ((MinecraftServerExtended) Objects.requireNonNull(this.level().getServer())).worldthreader$isTickMultithreaded()) {
            return ((ServerWorldWithWirelessRedstoneFix) this.level()).worldthreader$getNextEntityId(entityIdCounter);
        }
        return entityIdCounter.incrementAndGet();
    }
}
