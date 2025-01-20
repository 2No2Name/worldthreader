package no2.worldthreader.mixin.dimension_change.player.instance_swap;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerMixin {

    // Allow null level to be passed in ctor:
    @Redirect(
            method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z")
    )
    private boolean isClientSideAllowNull(Level instance) {
        if (instance == null) {
            //noinspection ConstantValue
            return !((Object) this instanceof ServerPlayer);
        }
        return instance.isClientSide;
    }
}
