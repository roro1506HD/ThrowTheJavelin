package fr.ekalia.minigames.throwthejavelin.game;

import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import fr.ekalia.minigames.throwthejavelin.game.type.GameDistance;
import fr.ekalia.minigames.throwthejavelin.game.type.GameTarget;
import fr.ekalia.minigames.throwthejavelin.game.type.IGameType;
import fr.ekalia.minigames.throwthejavelin.listener.ItemListener;
import fr.ekalia.minigames.throwthejavelin.util.LocationUtil;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import net.minecraft.server.v1_14_R1.PacketPlayInSteerVehicle;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityMetadata;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

/**
 * @author roro1506_HD
 */
public class GameManager {

    private final ThrowTheJavelin plugin;

    private final Object2ObjectMap<UUID, GamePlayer> playersByUuid;
    private final ItemStack[] inventoryTemplate;

    private final Location launchLocation;
    private final ArmorStand launchMount;
    private final ArmorStand spectatorCorpse;

    private final BufferedImage background;

    private GamePlayer[] playerRotation;
    private GamePlayer currentPlayer;
    private int currentPlayerIndex;
    private int round;

    private Trident currentJavelin;
    private int thrownJavelins;

    private IGameType[] gameRotation;
    private IGameType currentGame;
    private int currentGameIndex;

    private GameState gameState;

    public GameManager(ThrowTheJavelin plugin) {
        this.plugin = plugin;
        this.playersByUuid = new Object2ObjectOpenHashMap<>();

        this.inventoryTemplate = new ItemStack[36];

        ItemStack trident = new ItemStack(Material.TRIDENT);
        for (int i = 0; i < 9; i++)
            this.inventoryTemplate[i] = trident;

        if (!this.plugin.getGameConfig().checkConfig()) {
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            throw new IllegalStateException("Could not start plugin : It has been miss-configured");
        }

        this.launchLocation = this.plugin.getGameConfig().getLaunchLocation();

        this.launchMount = this.launchLocation.getWorld().spawn(this.launchLocation.clone().subtract(0, 1.5, 0), ArmorStand.class);
        this.launchMount.setGravity(false);
        this.launchMount.setVisible(false);

        Location spectatorLocation = this.plugin.getGameConfig().getSpecLocation();
        this.spectatorCorpse = spectatorLocation.getWorld().spawn(spectatorLocation, ArmorStand.class);
        this.spectatorCorpse.setGravity(false);
        this.spectatorCorpse.setVisible(false);

        this.gameRotation = new IGameType[]{new GameDistance(plugin), new GameTarget(plugin)};
        this.currentGame = this.gameRotation[0];
        this.gameState = GameState.WAITING;

        try {
            this.background = ImageIO.read(GameManager.class.getResourceAsStream("/hg7.png"));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        this.plugin.getPacketManager().addHandler(PacketPlayInSteerVehicle.class, event ->
        {
            if (this.gameState != GameState.IN_GAME)
                return;

            if (this.currentPlayer.getUuid().equals(event.getPlayer().getUuid()))
                event.setCancelled(true); // Cancel the packet. That means non-only it won't fuck the server, but it also will not make the player dismount the entity till we say so
        });
    }

    /**
     * Registers a player into the game system
     *
     * @param player the player
     */
    public void registerPlayer(GamePlayer player) {
        this.playersByUuid.put(player.getUuid(), player);
    }

    /**
     * Unregisters a player from the game system
     *
     * @param uuid the player's {@link UUID}
     */
    public void unregisterPlayer(UUID uuid) {
        GamePlayer gamePlayer = this.playersByUuid.remove(uuid);

        if (this.playerRotation != null && this.gameState == GameState.IN_GAME) {
            if (gamePlayer.equals(this.playerRotation[this.currentPlayerIndex]))
                this.nextPlayer();

            for (int i = 0; i < this.playerRotation.length; i++)
                if (gamePlayer.equals(this.playerRotation[i]))
                    this.playerRotation[i] = null;
        }
    }

    /**
     * Disables the Game Manager. Basically it kills the armor stand
     */
    public void disable() {
        Arrays.stream(this.gameRotation).forEach(IGameType::disable);
        this.launchMount.remove();
        this.spectatorCorpse.remove();
    }

    /**
     * Starts the game, used in the {@link fr.ekalia.minigames.throwthejavelin.command.StartCommand}
     */
    public void start() {
        this.currentGame.start();
        this.gameState = GameState.IN_GAME;
        this.playerRotation = this.getPlayers().toArray(new GamePlayer[0]);
        this.currentPlayerIndex = -1;

        // Hides each players to each other. Using this method to loop through half of what two loops would have done, https://fr.wikipedia.org/wiki/Analyse_de_la_complexit%C3%A9_des_algorithmes
        List<GamePlayer> players = new ArrayList<>(this.getPlayers());
        for (int i = 0; i < players.size(); i++)
            for (int j = 0; j < i; j++) {
                GamePlayer player = players.get(i);
                GamePlayer otherPlayer = players.get(j);

                if (!player.equals(otherPlayer)) {
                    player.getPlayer().hidePlayer(this.plugin, otherPlayer.getPlayer());
                    otherPlayer.getPlayer().hidePlayer(this.plugin, player.getPlayer());
                }
            }

        for (GamePlayer player : this.playersByUuid.values()) {
            player.getPlayer().teleport(this.plugin.getGameConfig().getSpecLocation());
            player.getPlayer().setGameMode(GameMode.SPECTATOR);

            if (!player.isSpectator())
                player.getPlayer().setSpectatorTarget(this.spectatorCorpse);
        }

        this.nextPlayer();

        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, () ->
        {
            if (this.currentJavelin == null)
                return;

            Location javelinLocation = this.currentJavelin.getLocation().clone();

            javelinLocation.setY(this.launchLocation.getY());

            this.spectatorCorpse.setVelocity(this.currentJavelin.getVelocity());
        }, 1L, 1L);
    }

    /**
     * Switch one player forward, making old player spectator
     */
    private void nextPlayer() {
        if (this.currentPlayerIndex != -1 && this.currentPlayer != null)
            for (GamePlayer player : this.playersByUuid.values())
                if (!player.isSpectator() && !player.equals(this.currentPlayer))
                    player.getPlayer().hidePlayer(this.plugin, this.currentPlayer.getPlayer());

        if (this.getPlayers().size() <= 1) {
            this.finish();
            return;
        }

        int delay = 0;

        do {
            if (++this.currentPlayerIndex == this.playerRotation.length) // Verify if we reach the end of the array, could be done with modulo but it doesn't return a boolean
            {
                int roundsCount = this.plugin.getGameConfig().getRoundsCount();
                if (++this.round % roundsCount == 0) // Increment the round, and finish if we reached the last one
                {
                    if (this.round / roundsCount == this.gameRotation.length) {
                        this.finish();
                        return;
                    } else {
                        this.currentGame = this.gameRotation[++this.currentGameIndex];
                        this.currentGame.start();
                    }
                }

                delay = this.currentGame.nextRound();

                this.currentPlayerIndex = 0; // Reset the index
                this.playerRotation = this.getPlayers().toArray(new GamePlayer[0]); // Refresh the player rotation to remove all possible nulls
            }
        } while ((this.currentPlayer = this.playerRotation[this.currentPlayerIndex]) == null);

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
        {
            Location specLocation = this.plugin.getGameConfig().getSpecLocation();

            this.spectatorCorpse.teleport(specLocation);

            for (GamePlayer player : this.playersByUuid.values()) {
                this.plugin.getScoreboardManager().updateRound(player);
                if (!player.isSpectator()) {
                    player.getPlayer().teleport(specLocation);

                    if (!player.equals(this.currentPlayer))
                        player.getPlayer().showPlayer(this.plugin, this.currentPlayer.getPlayer());
                }
            }

            this.currentGame.nextPlayer(this.currentPlayer);

            this.currentPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
            this.currentPlayer.getPlayer().getHandle().setInvisible(false); // Fix invisibility bug : Player was invisible to himself when he was riding the ArmorStand, after being spectator
            this.currentPlayer.sendPacket(new PacketPlayOutEntityMetadata(this.currentPlayer.getPlayer().getEntityId(), this.currentPlayer.getPlayer().getHandle().getDataWatcher(), false)); // Send DataWatcher to player, to fix the invisibility bug
            this.currentPlayer.getPlayer().teleport(this.launchLocation);

            this.currentPlayer.getPlayer().getInventory().setContents(this.inventoryTemplate);

            // Make player mount the ArmorStand, but stay stand up for all other players
            // Incoming riding packets will be filtered to not fuck the server up
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.currentPlayer.sendMountPacket(this.launchMount.getEntityId(), true), 1); // Delay the mount, it works half of the time without the delay
        }, delay);
    }

    public void onJavelinLand(Trident trident, GamePlayer thrower) {
        this.currentJavelin = null;

        this.spectatorCorpse.setGravity(false);

        try {
            this.currentGame.onJavelinLand(trident, thrower);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
        {
            trident.remove();

            thrower.getNPC().remove();

            this.plugin.getGameManager().getPlayers().forEach(this.plugin.getScoreboardManager()::updateLeaderBoard);
            this.nextPlayer();
        }, 20);
    }

    /**
     * Starts the end process. This makes the win effect, the leader board, the summary for each player and the overall best/worst stats
     */
    private void finish() {
        this.currentGame.disable();
        this.gameState = GameState.FINISHED;
        this.plugin.getServer().getScheduler().cancelTasks(this.plugin);

        this.getAllPlayers().forEach(player ->
        {
            if (!player.isSpectator()) {
                player.getPlayer().setGameMode(GameMode.ADVENTURE);
                this.currentPlayer.getPlayer().getHandle().setInvisible(false); // Fix invisibility bug : Players were invisible to themselves after being spectator
                this.currentPlayer.sendPacket(new PacketPlayOutEntityMetadata(this.currentPlayer.getPlayer().getEntityId(), this.currentPlayer.getPlayer().getHandle().getDataWatcher(), false)); // Send DataWatcher to player, to fix the invisibility bug
            }

            player.getPlayer().teleport(this.plugin.getGameConfig().getFinishLocation());
            player.getPlayer().getInventory().setItem(0, ItemListener.PLAYER_SHOW);
        });

        DecimalFormat format = new DecimalFormat("0.00");
        Location[] winnersLocations = {
                this.plugin.getGameConfig().getFirstLeaderboardLocation(),
                this.plugin.getGameConfig().getSecondLeaderboardLocation(),
                this.plugin.getGameConfig().getThirdLeaderboardLocation()
        };

        List<GamePlayer> winners = this.getPlayers().stream()
                .sorted(Comparator.comparingDouble(GamePlayer::getTotalScore).reversed())
                .limit(3)
                .collect(Collectors.toList());

        Arrays.stream(this.gameRotation).forEach(game -> game.finish(winners));

        for (int i = 0; i < 3; i++) {
            if (winners.size() == i)
                break;


            Location location = winnersLocations[i];

            Block block = location.getWorld().getBlockAt(LocationUtil.getLocation(location.clone(), 0.0F, -0.5D, 1.0D));
            block.setType(Material.OAK_WALL_SIGN);

            Sign sign = (Sign) block.getState();
            org.bukkit.block.data.type.WallSign signData = (org.bukkit.block.data.type.WallSign) block.getBlockData();

            GamePlayer winner = winners.get(i);

            sign.setEditable(false);
            sign.setLine(0, "=================");
            sign.setLine(1, winner.getName());
            sign.setLine(2, format.format(winner.getTotalScore()));
            sign.setLine(3, "=================");
            sign.update();

            signData.setFacing(LocationUtil.getDirection(location));
            block.setBlockData(signData, true);

            winner.getNPC().spawn(location);
        }
    }

    /**
     * @return the current {@link IGameType}
     */
    public IGameType getCurrentGame() {
        return this.currentGame;
    }

    /**
     * @return this game state
     */
    public GameState getGameState() {
        return this.gameState;
    }

    /**
     * @return the game round number
     */
    public int getRound() {
        return this.round;
    }

    /**
     * @return the current player index. Corresponds to {@link GameManager#playerRotation}
     */
    public int getCurrentPlayerIndex() {
        return this.currentPlayerIndex;
    }

    /**
     * @return the maximum value of {@link GameManager#currentPlayerIndex}
     */
    public int getMaxPlayerIndex() {
        return this.playerRotation.length;
    }

    /**
     * Sets the current javelin
     *
     * @param currentJavelin the trident which just been thrown
     */
    public void setCurrentJavelin(Trident currentJavelin) {
        if (currentJavelin != null)
            this.thrownJavelins++;

        this.currentJavelin = currentJavelin;
    }

    /**
     * @return the amount of thrown javelins
     */
    public int getThrownJavelins() {
        return this.thrownJavelins;
    }

    /**
     * @return the item frame background
     */
    public BufferedImage getBackground() {
        return this.background;
    }

    /**
     * @return Launch mount's entity id
     */
    public int getLaunchEntityId() {
        return this.launchMount.getEntityId();
    }

    /**
     * @return the spectator corpse used for the traveling
     */
    public ArmorStand getSpectatorCorpse() {
        return this.spectatorCorpse;
    }

    /**
     * Gets a {@link GamePlayer} from its {@link UUID}
     *
     * @param uuid the player's {@link UUID}
     * @return the player's {@link GamePlayer} instance
     */
    public GamePlayer getPlayer(UUID uuid) {
        return this.playersByUuid.get(uuid);
    }

    /**
     * @return all currently connected players's {@link GamePlayer} instances
     */
    public Set<GamePlayer> getAllPlayers() {
        return new HashSet<>(this.playersByUuid.values());
    }

    /**
     * @return all players {@link GamePlayer} instances
     */
    public Set<GamePlayer> getPlayers() {
        return this.playersByUuid.values().stream().filter(((Predicate<GamePlayer>) GamePlayer::isSpectator).negate()).collect(Collectors.toSet());
    }

    /**
     * @return all currently connected player's {@link GamePlayer} instances
     */
    public Set<GamePlayer> getSpectators() {
        return this.playersByUuid.values().stream().filter(GamePlayer::isSpectator).collect(Collectors.toSet());
    }
}
