package no2.worldthreader.mixin.dimension_change.player.instance_swap;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import no2.worldthreader.common.mixin_support.interfaces.TransparentServerPlayerSwapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements TransparentServerPlayerSwapper {

    @Shadow @Final private List<ServerPlayer> players;

    @Shadow @Final private MinecraftServer server;

    @Shadow @Final private Map<UUID, ServerPlayer> playersByUUID;

    @Override
    public ServerPlayer worldthreader$swapPlayerWithNewCopy(ServerPlayer previousPlayer) {
        //Copied from PlayerList.respawn, but removed a bunch of things that are wrong
        // bl = true (keep inventory or so, used when using end portals)
        // removalReason = Changed Dimension (least harmful, also we use it when changing dimension)

        this.players.remove(previousPlayer);
        previousPlayer.serverLevel().removePlayerImmediately(previousPlayer, Entity.RemovalReason.CHANGED_DIMENSION);
        TeleportTransition teleportTransition = null; // No teleport Transition
        ServerLevel newLevel = previousPlayer.serverLevel(); // Not from teleportTransition
        ServerPlayer newPlayer = new ServerPlayer(this.server, newLevel, previousPlayer.getGameProfile(), previousPlayer.clientInformation());
        newPlayer.connection = previousPlayer.connection;
        newPlayer.restoreFrom(previousPlayer, true);
        newPlayer.setId(previousPlayer.getId());
        newPlayer.setMainArm(previousPlayer.getMainArm());
        newPlayer.copyRespawnPosition(previousPlayer);

        for (String string : previousPlayer.getTags()) {
            newPlayer.addTag(string);
        }

        newLevel.addRespawnedPlayer(newPlayer);
        this.players.add(newPlayer);
        this.playersByUUID.put(newPlayer.getUUID(), newPlayer);
        newPlayer.initInventoryMenu();
        newPlayer.setHealth(newPlayer.getHealth());

        //Additional Stuff
        if (newPlayer.isChangingDimension() != previousPlayer.isChangingDimension()) {
            newPlayer.isChangingDimension = previousPlayer.isChangingDimension();
        }
        if (newPlayer.getPortalCooldown() != previousPlayer.getPortalCooldown()) {
            newPlayer.setPortalCooldown(previousPlayer.getPortalCooldown());
        }
        //Others like this might be relevant but hard to track down, not doing it for now
//        newPlayer.startingToFallPosition = previousPlayer.startingToFallPosition;
        //TODO check the set of enderpearls and the other somewhat important fields (which though?)

        return newPlayer;
    }

}
