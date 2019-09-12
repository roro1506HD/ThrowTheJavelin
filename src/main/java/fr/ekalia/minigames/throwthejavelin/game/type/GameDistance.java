package fr.ekalia.minigames.throwthejavelin.game.type;

import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import fr.ekalia.minigames.throwthejavelin.util.FontUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.PacketPlayOutTitle;
import org.bukkit.Location;
import org.bukkit.entity.Trident;

/**
 * @author roro1506_HD
 */
public class GameDistance implements IGameType {

    private final ThrowTheJavelin plugin;

    public GameDistance(ThrowTheJavelin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        PacketPlayOutTitle times = new PacketPlayOutTitle(10, 40, 10);
        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText("§eLancer en longueur"));
        PacketPlayOutTitle subtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText("§7Tirez le plus loin possible !"));

        for (GamePlayer player : this.plugin.getGameManager().getPlayers()) {
            player.sendPacket(times);
            player.sendPacket(title);
            player.sendPacket(subtitle);
        }
    }

    @Override
    public int nextRound() {
        // Unused here
        return 0;
    }

    @Override
    public void finish(List<GamePlayer> winners) {
        DecimalFormat format = new DecimalFormat("0.00");

        format.setRoundingMode(RoundingMode.FLOOR);

        this.plugin.getMapManager().createMapWall(this.plugin.getGameConfig().getPlayerStatsLocation(), 3, 3, (image, player) ->
        {
            Graphics graphics = image.getGraphics();

            graphics.drawImage(this.plugin.getGameManager().getBackground(), 0, 0, null);

            graphics.setColor(Color.WHITE);
            graphics.setFont(FontUtil.OPEN_SANS.deriveFont(Font.BOLD, 25F));

            String text;
            graphics.drawString(text = "LANCER EN LONGUEUR", 198 - graphics.getFontMetrics().stringWidth(text) / 2, 30);

            graphics.setFont(FontUtil.OPEN_SANS.deriveFont(20F));

            if (player.isSpectator()) {
                graphics.drawString(text = "Vous êtes un spectateur,", 198 - graphics.getFontMetrics().stringWidth(text) / 2, 100);
                graphics.drawString(text = "pourquoi êtes-vous en train", 198 - graphics.getFontMetrics().stringWidth(text) / 2, 120);
                graphics.drawString(text = "d'essayer de voir vos statistiques ?", 198 - graphics.getFontMetrics().stringWidth(text) / 2, 140);

                graphics.dispose();
                return;
            }

            for (int i = 0; i < this.plugin.getGameManager().getRound() / 2; i++)
                graphics.drawString(text = "Lancer n°" + (i + 1) + " : " + format.format(player.getSavedJavelinDistances().get(i)) + "m", 198 - graphics.getFontMetrics().stringWidth(text) / 2, 100 + i * 35);

            graphics.dispose();
        });

        GamePlayer farthestThrower = null;
        GamePlayer closestThrower = null;
        double farthestJavelin = -1.0D;
        double closestJavelin = Double.MAX_VALUE;

        for (GamePlayer player : this.plugin.getGameManager().getPlayers()) {
            List<Double> thrownJavelins = player.getSavedJavelinDistances();

            for (double distance : thrownJavelins)
                if (distance > farthestJavelin) {
                    farthestJavelin = distance;
                    farthestThrower = player;
                } else if (distance < closestJavelin) {
                    closestJavelin = distance;
                    closestThrower = player;
                }
        }

        final GamePlayer finalFarthestThrower = farthestThrower;
        final GamePlayer finalClosestThrower = closestThrower;
        final double finalFarthestJavelin = farthestJavelin;
        final double finalClosestJavelin = closestJavelin;

        this.plugin.getMapManager().createMapWall(this.plugin.getGameConfig().getOverallStatsLocation(), 3, 3, (image, player) ->
        {
            Graphics graphics = image.getGraphics();

            graphics.drawImage(this.plugin.getGameManager().getBackground(), 0, 0, null);

            graphics.setColor(Color.WHITE);
            graphics.setFont(FontUtil.OPEN_SANS.deriveFont(Font.BOLD, 25F));

            String text;
            graphics.drawString(text = "STATISTIQUES GLOBALES", 198 - graphics.getFontMetrics().stringWidth(text) / 2, 30);

            graphics.setFont(FontUtil.OPEN_SANS.deriveFont(20F));

            graphics.drawString(text = "Lancers totaux : " + this.plugin.getGameManager().getThrownJavelins(), 198 - graphics.getFontMetrics().stringWidth(text) / 2, 100);
            graphics.drawString(text = "Plus long lancer : " + format.format(finalFarthestJavelin) + "m", 198 - graphics.getFontMetrics().stringWidth(text) / 2, 135);
            graphics.drawString(text = "(Par " + finalFarthestThrower.getName() + ")", 198 - graphics.getFontMetrics().stringWidth(text) / 2, 155);
            graphics.drawString(text = "Plus court lancer : " + format.format(finalClosestJavelin) + "m", 198 - graphics.getFontMetrics().stringWidth(text) / 2, 190);
            graphics.drawString(text = "(Par " + finalClosestThrower.getName() + ")", 198 - graphics.getFontMetrics().stringWidth(text) / 2, 210);

            graphics.dispose();
        });
    }

    @Override
    public void nextPlayer(GamePlayer player) {
        // Unused here
    }

    @Override
    public void disable() {
        // Unused here
    }

    @Override
    public void onJavelinLand(Trident trident, GamePlayer thrower) {
        Location launchLocation = this.plugin.getGameConfig().getLaunchLocation().clone();

        launchLocation.setY(trident.getLocation().getY());

        thrower.setLastScore(launchLocation.distance(trident.getLocation()));
        thrower.saveJavelinDistance(thrower.getLastScore());
        thrower.updateTotalScore();

        this.plugin.getScoreboardManager().updateScore(thrower);
    }
}
