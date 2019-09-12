package fr.ekalia.minigames.throwthejavelin.game.player;

import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import fr.ekalia.minigames.throwthejavelin.game.GameState;
import fr.ekalia.minigames.throwthejavelin.npc.HumanNPC;
import fr.ekalia.minigames.throwthejavelin.util.GameScoreboard;
import fr.ekalia.minigames.throwthejavelin.util.Vector2d;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.server.v1_14_R1.Packet;
import net.minecraft.server.v1_14_R1.PacketPlayOutMount;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.doubles.DoubleList;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @author roro1506_HD
 */
public class GamePlayer {

    private final ThrowTheJavelin plugin;
    private final UUID uuid;
    private final CraftPlayer player;
    private final GameScoreboard scoreboard;
    private final HumanNPC npc;
    private final DoubleList savedJavelinDistances;
    private final ObjectList<Vector2d> targetJavelinLocations;

    private boolean spectator;

    private double lastScore;
    private double totalScore;

    public GamePlayer(ThrowTheJavelin plugin, Player player) {
        this.plugin = plugin;
        this.uuid = player.getUniqueId();
        this.player = (CraftPlayer) player;
        this.scoreboard = new GameScoreboard(this, "§e§lLANCER DE JAVELOT");
        this.npc = new HumanNPC(plugin, this);
        this.savedJavelinDistances = new DoubleArrayList();
        this.targetJavelinLocations = new ObjectArrayList<>();
        this.spectator = player.isOp();

        if (plugin.getGameManager().getGameState() != GameState.WAITING && player.isOp())
            player.setGameMode(GameMode.SPECTATOR);
        else
            player.setGameMode(GameMode.ADVENTURE);
    }

    /**
     * Sends mount packet to player, making him ride the entity only on his screen
     *
     * @param entityId The vehicle
     * @param passenger If this player is going to be passenger or not
     */
    public void sendMountPacket(int entityId, boolean passenger) {
        try {
            PacketPlayOutMount packet = new PacketPlayOutMount();
            Field field = packet.getClass().getDeclaredField("a");
            field.setAccessible(true);
            field.set(packet, entityId);

            field = packet.getClass().getDeclaredField("b");
            field.setAccessible(true);

            if (passenger)
                field.set(packet, new int[]{this.player.getEntityId()});
            else
                field.set(packet, new int[0]);

            this.sendPacket(packet);
        } catch (Exception ex) {
            this.plugin.log(ex);
        }
    }

    /**
     * Send a packet to this player
     *
     * @param packet the packet to send
     */
    public void sendPacket(Packet<?> packet) {
        if (packet != null)
            this.player.getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * Updates the total javelin distance. Basically, it just adds the last javelin distance to the current total distance
     */
    public void updateTotalScore() {
        this.totalScore += this.lastScore;
    }

    /**
     * @return all the stored throws
     */
    public List<Double> getSavedJavelinDistances() {
        return this.savedJavelinDistances;
    }

    /**
     * Saves a just-thrown javelin distance
     *
     * @param distance the distance
     * @see fr.ekalia.minigames.throwthejavelin.game.type.GameDistance
     */
    public void saveJavelinDistance(double distance) {
        this.savedJavelinDistances.add(distance);
    }

    /**
     * @return all the previously saved javelin locations relative to target center
     */
    public List<Vector2d> getTargetJavelinLocations() {
        return this.targetJavelinLocations;
    }

    /**
     * Saves a target javelin location relative to center of the target
     *
     * @param vector2d The offset by the center of the target
     * @see fr.ekalia.minigames.throwthejavelin.game.type.GameTarget
     */
    public void saveJavelinLocation(Vector2d vector2d) {
        this.targetJavelinLocations.add(vector2d);
    }

    /**
     * @return the last score of this player
     */
    public double getLastScore() {
        return this.lastScore;
    }

    /**
     * Sets the last score
     *
     * @param lastScore the score
     */
    public void setLastScore(double lastScore) {
        this.lastScore = lastScore;
    }

    /**
     * @return the total of all thrown javelins distances
     */
    public double getTotalScore() {
        return this.totalScore;
    }

    /**
     * @return this player's NPC instance
     */
    public HumanNPC getNPC() {
        return this.npc;
    }

    /**
     * @return this player's no-flicker scoreboard instance
     */
    public GameScoreboard getScoreboard() {
        return this.scoreboard;
    }

    /**
     * @return the player's name
     */
    public String getName() {
        return this.player.getName();
    }

    /**
     * @return the player's {@link UUID}
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * @return {@code true} if this player is spectating, otherwise returns {@code false}
     */
    public boolean isSpectator() {
        return this.spectator;
    }

    /**
     * @return the player's instance
     * @see Player
     * @see CraftPlayer
     */
    public CraftPlayer getPlayer() {
        return this.player;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof GamePlayer))
            return false;

        GamePlayer that = (GamePlayer) o;
        return Objects.equals(this.uuid, that.uuid);
    }
}
