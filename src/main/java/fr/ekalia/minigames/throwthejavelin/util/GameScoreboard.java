package fr.ekalia.minigames.throwthejavelin.util;

import com.google.common.base.Preconditions;
import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import java.util.Collections;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.ChatColor;

/**
 * @author roro1506_HD
 */
public class GameScoreboard extends QuickReflection {

    private final String[] lines;
    private final GamePlayer player;

    private String objectiveName;
    private boolean created;

    public GameScoreboard(GamePlayer player, String objectiveName) {
        this.lines = new String[16];
        this.player = player;
        this.objectiveName = objectiveName;
    }

    /**
     * Creates the scoreboard and display it to the player
     */
    public void create() {
        if (this.created)
            return;

        this.player.sendPacket(this.getObjectivePacket(0));
        this.player.sendPacket(this.getDisplayObjectivePacket());

        String line;
        for (int i = 0; i < this.lines.length; i++)
            if ((line = this.lines[i]) != null)
                this.createLine(i, line);

        this.created = true;
    }

    /**
     * Destroys the scoreboard and hide it from the player
     */
    public void destroy() {
        if (!this.created)
            return;

        for (int i = 0; i < this.lines.length; i++)
            this.removeLine(i);

        this.player.sendPacket(this.getObjectivePacket(1));

        this.created = false;
    }

    /**
     * Sets the specified line to the specified {@link String}
     *
     * @param index The index of the line, from top to bottom
     * @param line The text of the line
     * @throws IndexOutOfBoundsException if not between 0 and 15 (inclusive)
     */
    public void setLine(int index, String line) {
        Preconditions.checkPositionIndex(index, this.lines.length);
        String old = this.lines[index];

        this.lines[index] = line;

        if (this.created)
            if (line == null)
                this.removeLine(index);
            else if (old == null)
                this.createLine(index, line);
            else
                this.updateLine(index, line);
    }

    /**
     * Retrieves the {@link String} for the specified line index
     *
     * @param index The index of the line, from top to bottom
     * @return the text of the specified line
     * @throws IndexOutOfBoundsException if not between 0 and 15 (inclusive)
     */
    public String getLine(int index) {
        Preconditions.checkPositionIndex(index, this.lines.length);
        return this.lines[index];
    }

    /**
     * Internal method to create a line.
     * <p>
     * Sends both required packets to show the line, the player and the team
     */
    private void createLine(int index, String line) {
        this.player.sendPacket(this.getTeamPacket(index, line, 0));
        this.player.sendPacket(this.getScorePacket(ScoreboardServer.Action.CHANGE, index));
    }

    /**
     * Internal method to update a line.
     * <p>
     * Sends the required packet to update the team
     */
    private void updateLine(int index, String line) {
        this.player.sendPacket(this.getTeamPacket(index, line, 2));
    }

    /**
     * Internal method to remove a line
     * <p>
     * Sends both required packets to remove the line and the team
     */
    private void removeLine(int index) {
        this.player.sendPacket(this.getScorePacket(ScoreboardServer.Action.REMOVE, index));
        this.player.sendPacket(this.getTeamPacket(index, null, 1));
    }

    /**
     * Internal method to create a {@link PacketPlayOutScoreboardObjective}
     */
    private PacketPlayOutScoreboardObjective getObjectivePacket(int mode) {
        PacketPlayOutScoreboardObjective packet = new PacketPlayOutScoreboardObjective();

        setFieldValue(packet, "a", this.player.getName());
        setFieldValue(packet, "b", new ChatComponentText(this.objectiveName));
        setFieldValue(packet, "c", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);
        setFieldValue(packet, "d", mode);

        return packet;
    }

    /**
     * Internal method to create a {@link PacketPlayOutScoreboardDisplayObjective}
     */
    private PacketPlayOutScoreboardDisplayObjective getDisplayObjectivePacket() {
        PacketPlayOutScoreboardDisplayObjective packet = new PacketPlayOutScoreboardDisplayObjective();

        setFieldValue(packet, "a", 1);
        setFieldValue(packet, "b", this.player.getName());

        return packet;
    }

    /**
     * Internal method to create a {@link PacketPlayOutScoreboardTeam}
     */
    private PacketPlayOutScoreboardTeam getTeamPacket(int index, String prefix, int mode) {
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();

        setFieldValue(packet, "a", "faketeam_" + index);
        setFieldValue(packet, "b", new ChatComponentText("faketeam_" + index));
        setFieldValue(packet, "c", prefix == null ? null : new ChatComponentText(prefix));
        setFieldValue(packet, "d", new ChatComponentText(""));
        setFinalFieldValue(packet, "h", Collections.singletonList(ChatColor.values()[index].toString()));
        setFieldValue(packet, "i", mode);
        setFieldValue(packet, "j", 0);

        return packet;
    }

    /**
     * Internal method to create a {@link PacketPlayOutScoreboardScore}
     */
    private PacketPlayOutScoreboardScore getScorePacket(ScoreboardServer.Action action, int index) {
        PacketPlayOutScoreboardScore packet = new PacketPlayOutScoreboardScore();

        setFieldValue(packet, "a", ChatColor.values()[index].toString());
        setFieldValue(packet, "b", this.player.getName());
        setFieldValue(packet, "c", 15 - index);
        setFieldValue(packet, "d", action);

        return packet;
    }
}
