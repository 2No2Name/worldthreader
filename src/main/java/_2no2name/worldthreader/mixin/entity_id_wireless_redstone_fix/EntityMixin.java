package _2no2name.worldthreader.mixin.entity_id_wireless_redstone_fix;

import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import _2no2name.worldthreader.common.mixin_support.interfaces.ServerWorldWithWirelessRedstoneFix;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Entity.class)
public class EntityMixin {

    @Shadow
    public World world;

    @Redirect(
            method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/atomic/AtomicInteger;incrementAndGet()I", remap = false)
    )
    private int getNextEntityIDThreaded(AtomicInteger entityIdCounter) {
        if (this.world instanceof ServerWorld && ((MinecraftServerExtended) Objects.requireNonNull(this.world.getServer())).isTickMultithreaded()) {
            ((ServerWorldWithWirelessRedstoneFix) this.world).getNextEntityId(entityIdCounter);
        }
        return entityIdCounter.incrementAndGet();
    }
}
