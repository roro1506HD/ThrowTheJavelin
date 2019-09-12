package fr.ekalia.minigames.throwthejavelin.packet;

import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import java.lang.reflect.Field;
import net.minecraft.server.v1_14_R1.Packet;

/**
 * @author roro1506_HD
 */
public class PacketEvent<T extends Packet> {

    private final T packet;
    private final GamePlayer player;

    private boolean cancelled;

    PacketEvent(T packet, GamePlayer player) {
        this.packet = packet;
        this.player = player;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public T getPacket() {
        return this.packet;
    }

    public GamePlayer getPlayer() {
        return this.player;
    }

    public <U> U getField(Class<U> clazz, String fieldName) {
        try {
            Field field = this.packet.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return clazz.cast(field.get(this.packet));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setField(String fieldName, Object value) {
        try {
            Field field = this.packet.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            field.set(this.packet, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
