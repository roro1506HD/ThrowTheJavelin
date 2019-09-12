package fr.ekalia.minigames.throwthejavelin.npc;

import com.mojang.authlib.GameProfile;
import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import fr.ekalia.minigames.throwthejavelin.util.QuickReflection;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.UUID;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Location;

/**
 * @author roro1506_HD
 */
public class HumanNPC extends QuickReflection {

    private final ThrowTheJavelin plugin;
    private final int entityId;
    private final GamePlayer player;
    private final GameProfile profile;

    public HumanNPC(ThrowTheJavelin plugin, GamePlayer player) {
        this.plugin = plugin;
        this.entityId = (int) -(Math.ceil(Math.random() * 1000) + 2000);
        this.player = player;

        this.profile = new GameProfile(UUID.randomUUID(), this.player.getName());
        this.profile.getProperties().putAll(this.player.getPlayer().getHandle().getProfile().getProperties());
    }

    /**
     * Spawns an Human NPC wherever the player is, looking the same direction, with all the same data
     */
    public void spawn(Location location) {
        Packet<?>[] packets = {
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER),
                new PacketPlayOutNamedEntitySpawn(),
                new PacketPlayOutEntityHeadRotation()
        };

        setFieldCastValue(packets[0], "b", Collections.singletonList(this.getPlayerInfoData((PacketPlayOutPlayerInfo) packets[0])));

        setFieldValue(packets[1], "a", this.entityId); // Just change entity id to prevent client-side problems
        setFieldValue(packets[1], "b", this.profile.getId());
        setFieldValue(packets[1], "c", location.getX());
        setFieldValue(packets[1], "d", location.getY());
        setFieldValue(packets[1], "e", location.getZ());
        setFieldValue(packets[1], "f", (byte) (location.getYaw() * 256.0F / 360.0F));
        setFieldValue(packets[1], "g", (byte) (location.getPitch() * 256.0F / 360.0F));
        setFieldValue(packets[1], "h", this.player.getPlayer().getHandle().getDataWatcher());

        setFieldValue(packets[2], "a", this.entityId);
        setFieldValue(packets[2], "b", (byte) (location.getYaw() * 256.0F / 360.0F));

        for (GamePlayer player : this.plugin.getGameManager().getAllPlayers())
            for (Packet<?> packet : packets)
                player.sendPacket(packet);
    }

    /**
     * Removes the NPC
     */
    public void remove() {
        Packet<?>[] packets = {
                new PacketPlayOutEntityDestroy(this.entityId),
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER)
        };

        setFieldCastValue(packets[1], "b", Collections.singletonList(this.getPlayerInfoData((PacketPlayOutPlayerInfo) packets[1])));

        for (GamePlayer player : this.plugin.getGameManager().getAllPlayers())
            for (Packet<?> packet : packets)
                player.sendPacket(packet);
    }

    private Object getPlayerInfoData(PacketPlayOutPlayerInfo parent) {
        try {
            Constructor constructor = Class.forName("net.minecraft.server.v1_14_R1.PacketPlayOutPlayerInfo$PlayerInfoData").getDeclaredConstructor(PacketPlayOutPlayerInfo.class, GameProfile.class, int.class, EnumGamemode.class, IChatBaseComponent.class);
            return constructor.newInstance(parent, this.profile, 1, EnumGamemode.ADVENTURE, new ChatComponentText("§8[NPC] " + this.profile.getName()));
        } catch (Exception ex) {
            this.plugin.log(ex);
        }
        throw new IllegalStateException("This is not supposed to happen");
    }
}
