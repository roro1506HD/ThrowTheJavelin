package fr.ekalia.minigames.throwthejavelin.game.type;

import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import fr.ekalia.minigames.throwthejavelin.util.Vector2d;
import fr.ekalia.minigames.throwthejavelin.util.structure.PatternStructure;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.PacketPlayOutTitle;
import org.bukkit.Location;
import org.bukkit.entity.Trident;

/**
 * @author roro1506_HD
 */
public class GameTarget implements IGameType {

    private final ThrowTheJavelin plugin;
    private final Random random;
    private final PatternStructure targetStructure;
    private final Location minTargetLocation;
    private final Location maxTargetLocation;
    private final Location specCorpseOffset;

    private final BufferedImage target;
    private final BufferedImage targetCross;

    private Location targetLocation;

    public GameTarget(ThrowTheJavelin plugin) {
        this.plugin = plugin;
        this.random = new Random();

        this.targetStructure = PatternStructure.builder(plugin,
                "  wwwww  ",
                " wbbbbbw ",
                "wbbcccbbw",
                "wbcrrrcbw",
                "wbcryrcbw",
                "wbcrrrcbw",
                "wbbcccbbw",
                " wbbbbbw ",
                "  wwwww  ")
                .ingredient('w', "minecraft:white_concrete")
                .ingredient('b', "minecraft:black_concrete")
                .ingredient('c', "minecraft:cyan_concrete")
                .ingredient('r', "minecraft:red_concrete")
                .ingredient('y', "minecraft:yellow_concrete")
                .build();

        this.minTargetLocation = plugin.getGameConfig().getMinTargetLocation();
        this.maxTargetLocation = plugin.getGameConfig().getMaxTargetLocation();
        this.specCorpseOffset = plugin.getGameConfig().getSpecLocation().clone().subtract(plugin.getGameConfig().getLaunchLocation());

        try {
            this.target = ImageIO.read(GameTarget.class.getResourceAsStream("/target.png"));
            this.targetCross = ImageIO.read(GameTarget.class.getResourceAsStream("/target_cross.png"));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        if (this.minTargetLocation.getBlockY() != this.maxTargetLocation.getBlockY())
            throw new IllegalStateException("Min Target Location Y and Max Target Location Y must be at the same level");

        if (this.minTargetLocation.getWorld() != this.maxTargetLocation.getWorld())
            throw new IllegalStateException("Min Target Location World and Max Target Location World must be the same world");
    }

    @Override
    public void start() {
        PacketPlayOutTitle times = new PacketPlayOutTitle(10, 40, 10);
        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText("§eLancer sur cible"));
        PacketPlayOutTitle subtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText("§7Visez le centre de la cible !"));

        for (GamePlayer player : this.plugin.getGameManager().getPlayers()) {
            player.sendPacket(times);
            player.sendPacket(title);
            player.sendPacket(subtitle);
        }
    }

    @Override
    public int nextRound() {
        if (this.targetStructure.isPasted())
            this.targetStructure.undo();

        int x = this.minTargetLocation.getBlockX() + random.nextInt(this.maxTargetLocation.getBlockX() - this.minTargetLocation.getBlockX());
        int z = this.minTargetLocation.getBlockZ() + random.nextInt(this.maxTargetLocation.getBlockZ() - this.minTargetLocation.getBlockZ());

        this.targetStructure.paste(this.targetLocation = new Location(this.minTargetLocation.getWorld(), x, this.minTargetLocation.getBlockY(), z));

        Location location = this.targetLocation.clone().add(this.specCorpseOffset);

        location.setYaw(this.specCorpseOffset.getYaw());
        location.setPitch(this.specCorpseOffset.getPitch());

        this.plugin.getGameManager().getSpectatorCorpse().teleport(location);
        this.plugin.getGameManager().getPlayers().forEach(player -> player.getPlayer().teleport(location));

        return 20;
    }

    @Override
    public void finish(List<GamePlayer> winners) {
        this.plugin.getMapManager().createMapWall(this.plugin.getGameConfig().getTargetStatsLocation(), 3, 3, (image, player) ->
        {
            Graphics graphics = image.getGraphics();

            graphics.drawImage(this.plugin.getGameManager().getBackground(), 0, 0, null);
            graphics.drawImage(this.target, 0, 0, null);

            if (player.isSpectator()) {
                graphics.dispose();
                return;
            }

            for (Vector2d location : player.getTargetJavelinLocations())
                graphics.drawImage(this.targetCross, 194 - (int) (location.getX() * 25), 194 - (int) (location.getY() * 25), null);

            graphics.dispose();
        });
    }

    @Override
    public void nextPlayer(GamePlayer player) {

    }

    @Override
    public void disable() {
        if (this.targetStructure.isPasted())
            this.targetStructure.undo();
    }

    @Override
    public void onJavelinLand(Trident trident, GamePlayer thrower) {
        // Save location for stats
        thrower.saveJavelinLocation(new Vector2d(trident.getLocation().getX() - this.targetLocation.getX(), trident.getLocation().getZ() - this.targetLocation.getZ()));

        Location centerLocation = this.targetLocation.clone();

        centerLocation.setY(trident.getLocation().getY());
        centerLocation.add(0.5, 0, 0.5);

        double distance = centerLocation.distance(trident.getLocation());

        thrower.setLastScore(40 * Math.min(1, Math.exp(-Math.log10(distance))));
        thrower.updateTotalScore();
        this.plugin.getScoreboardManager().updateScore(thrower);
    }
}
