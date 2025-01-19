package no2.worldthreader.mixin.dimension_change.player.instance_swap;

import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import no2.worldthreader.common.mixin_support.interfaces.TransparentServerPlayerSwapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl
        implements ServerGamePacketListener,
        TransparentServerPlayerSwapper,
        ServerPlayerConnection,
        TickablePacketListener
{

    @Shadow public ServerPlayer player;


    public ServerGamePacketListenerImplMixin(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    @Override
    public ServerPlayer worldthreader$swapPlayerWithNewCopy(ServerPlayer previous) {
        if (this.player != previous)
            throw new IllegalArgumentException("Players not matching before player swap.");
        this.player = ((TransparentServerPlayerSwapper)this.server.getPlayerList()).worldthreader$swapPlayerWithNewCopy(this.player);
        return this.player;
    }
}
