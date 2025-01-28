package no2.worldthreader.mixin.dimension_change.player.instance_swap;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import no2.worldthreader.common.mixin_support.interfaces.ServerPlayerInstanceSwapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements ServerPlayerInstanceSwapper {

    @Shadow @Final private List<ServerPlayer> players;

    @Shadow @Final private MinecraftServer server;

    @Shadow @Final private Map<UUID, ServerPlayer> playersByUUID;

    @Override
    public ServerPlayer worldthreader$swapRemovedPlayerWithNewCopy(ServerPlayer previousPlayer, ServerLevel newLevel) {
        //Copied from PlayerList.respawn, but removed a bunch of things that are wrong
        // bl = true (keep inventory or so, used when using end portals)
        // removalReason = Changed Dimension (least harmful, also we use it when changing dimension)

        boolean removedFromPlayerList = this.players.remove(previousPlayer);

        //Create the player with a null level, which is set a bit later. The constructor uses the level to adjust the spawn position etc., which is not needed when copying the player.
        //noinspection DataFlowIssue
        ServerPlayer newPlayer = new ServerPlayer(this.server, null, previousPlayer.getGameProfile(), previousPlayer.clientInformation());

        newPlayer.connection = previousPlayer.connection;
        newPlayer.restoreFrom(previousPlayer, true);
        newPlayer.setId(previousPlayer.getId());
        newPlayer.setMainArm(previousPlayer.getMainArm());
        newPlayer.copyRespawnPosition(previousPlayer);

        for (String string : previousPlayer.getTags()) {
            newPlayer.addTag(string);
        }

        if (removedFromPlayerList) {
            this.players.add(newPlayer);
        }
        if (this.playersByUUID.containsKey(newPlayer.getUUID())) {
            this.playersByUUID.put(newPlayer.getUUID(), newPlayer);
        }

        //Loot context or so needs the minecraft server and the random instance from the level
        newPlayer.setServerLevel(newLevel);
        newPlayer.initInventoryMenu();
        newPlayer.setServerLevel(null);

        newPlayer.setHealth(newPlayer.getHealth());

        //Additional Stuff
        if (newPlayer.isChangingDimension() != previousPlayer.isChangingDimension()) {
            newPlayer.isChangingDimension = previousPlayer.isChangingDimension();
        }
        if (newPlayer.getPortalCooldown() != previousPlayer.getPortalCooldown()) {
            newPlayer.setPortalCooldown(previousPlayer.getPortalCooldown());
        }
        if (newPlayer.enderPearls.isEmpty() && !previousPlayer.enderPearls.isEmpty()) {
            for (var enderpearl : previousPlayer.enderPearls) {
                newPlayer.registerEnderPearl(enderpearl);
                //Setting the owner in the enderpearl might be a good idea as well, but since worldthreader
                // modifies the getOwner function such that the outdated previous value is immediately replaced on
                // access, it doesn't make a difference (unless other mods directly use the field)
            }
        }

        //Others fields like this might be relevant but hard to track down, not doing it for now
//        newPlayer.startingToFallPosition = previousPlayer.startingToFallPosition;

        return newPlayer;
    }

}
