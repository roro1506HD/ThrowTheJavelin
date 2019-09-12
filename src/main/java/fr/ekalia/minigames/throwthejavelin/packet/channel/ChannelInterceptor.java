package fr.ekalia.minigames.throwthejavelin.packet.channel;

import fr.ekalia.minigames.throwthejavelin.packet.PacketManager;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_14_R1.Packet;
import org.bukkit.entity.Player;

/**
 * @author roro1506_HD
 */
public class ChannelInterceptor extends ChannelDuplexHandler {

    private final PacketManager packetManager;
    private final Player player;

    public ChannelInterceptor(PacketManager packetManager, Player player) {
        this.packetManager = packetManager;
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!this.packetManager.handlePacket((Packet) msg, this.player.getUniqueId()))
            super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!this.packetManager.handlePacket((Packet) msg, this.player.getUniqueId()))
            super.write(ctx, msg, promise);
    }
}
