package no2.worldthreader.mixin.dimension_change.recover_failure;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import no2.worldthreader.common.dimension_change.TeleportedEntityInfo;
import no2.worldthreader.common.mixin_support.interfaces.EntityExtended;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements EntityExtended {


    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Shadow
    public abstract @Nullable ServerPlayer teleport(TeleportTransition teleportTransition);

    @Shadow
    public abstract ServerLevel level();

    @Override
    public void worldthreader$restoreEntity(TeleportedEntityInfo teleportedEntity) {
        //Unsure if the transition is right, but since startRiding is called immediately, being wrong probably doesn't do anything
        this.teleport(new TeleportTransition(this.level(), this.position(), Vec3.ZERO, this.getYRot(), this.getXRot(), false, true, Set.of(), TeleportTransition.DO_NOTHING));
    }
}
