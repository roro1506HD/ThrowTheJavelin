package fr.ekalia.minigames.throwthejavelin.packet;

import net.minecraft.server.v1_14_R1.Packet;

/**
 * @author roro1506_HD
 */
@FunctionalInterface
public interface IPacketHandler<T extends Packet> {

    void handle(PacketEvent<T> event);

}
