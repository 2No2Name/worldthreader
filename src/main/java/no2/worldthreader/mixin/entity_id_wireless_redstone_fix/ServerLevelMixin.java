package no2.worldthreader.mixin.entity_id_wireless_redstone_fix;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.mixin_support.interfaces.ServerWorldWithWirelessRedstoneFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Shadow
    public abstract MinecraftServer getServer();

    @Redirect(
            method = "getNextEntityId", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/atomic/AtomicInteger;incrementAndGet()I")
    )
    private int getNextEntityIDThreaded(AtomicInteger entityIdCounter) {
        if (((MinecraftServerExtended) Objects.requireNonNull(this.getServer())).worldthreader$isTickMultithreaded()) {
            return ((ServerWorldWithWirelessRedstoneFix) this).worldthreader$getNextEntityId(entityIdCounter);
        } //TODO this probably doesn't work for server players which have null level at start
        return entityIdCounter.incrementAndGet();
    }
}
