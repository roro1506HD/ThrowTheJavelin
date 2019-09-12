package fr.ekalia.minigames.throwthejavelin.packet;

import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import fr.ekalia.minigames.throwthejavelin.packet.channel.ChannelInterceptor;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.v1_14_R1.Packet;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @author roro1506_HD
 */
public class PacketManager {

    private final ThrowTheJavelin plugin;
    private final Object2ObjectMap<Class, Set<IPacketHandler>> handlers;

    public PacketManager(ThrowTheJavelin plugin) {
        this.plugin = plugin;
        this.handlers = new Object2ObjectOpenHashMap<>();
    }

    public <T extends Packet> void addHandler(Class<T> packetClass, IPacketHandler<T> handler) {
        this.handlers.computeIfAbsent(packetClass, unused -> new HashSet<>()).add(handler);
    }

    public void addChannel(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", "ekalia_packet_handler", new ChannelInterceptor(this, player));
    }

    public <T extends Packet> boolean handlePacket(T packet, UUID uuid) {
        Set<IPacketHandler> handlers = this.handlers.get(packet.getClass());

        if (handlers == null)
            return false;

        PacketEvent<T> event = new PacketEvent<>(packet, this.plugin.getGameManager().getPlayer(uuid));

        for (IPacketHandler handler : handlers)
            // noinspection unchecked
            handler.handle(event);

        return event.isCancelled();
    }
}
