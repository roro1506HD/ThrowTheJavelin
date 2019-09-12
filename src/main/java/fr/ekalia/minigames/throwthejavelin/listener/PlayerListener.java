package fr.ekalia.minigames.throwthejavelin.listener;

import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import fr.ekalia.minigames.throwthejavelin.game.GameState;
import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * @author roro1506_HD
 */
public class PlayerListener implements Listener {

    private final ThrowTheJavelin plugin;
    private final Location launchLocation;

    public PlayerListener(ThrowTheJavelin plugin) {
        this.plugin = plugin;
        this.launchLocation = plugin.getGameConfig().getLaunchLocation();
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!event.getPlayer().isOp() && this.plugin.getGameManager().getGameState() != GameState.WAITING)
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "La partie est déjà commencée");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        GamePlayer player = new GamePlayer(this.plugin, event.getPlayer());

        this.plugin.getGameManager().registerPlayer(player);
        this.plugin.getGameManager().getPlayers().forEach(tempPlayer ->
        {
            this.plugin.getScoreboardManager().updatePlayers(tempPlayer);
            this.plugin.getScoreboardManager().updateLeaderBoard(tempPlayer);
        });

        this.plugin.getScoreboardManager().initScoreboard(player);

        this.plugin.getPacketManager().addChannel(event.getPlayer());
        event.getPlayer().teleport(this.plugin.getGameConfig().getLobbyLocation());
        event.getPlayer().getInventory().clear();
        event.setJoinMessage("");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage("");

        if (this.plugin.getGameManager().getGameState() == GameState.FINISHED)
            return;

        this.plugin.getGameManager().unregisterPlayer(event.getPlayer().getUniqueId());
        this.plugin.getGameManager().getPlayers().forEach(tempPlayer ->
        {
            this.plugin.getScoreboardManager().updatePlayers(tempPlayer);
            this.plugin.getScoreboardManager().updateLeaderBoard(tempPlayer);
        });
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (this.plugin.getGameManager().getSpectatorCorpse().equals(event.getPlayer().getSpectatorTarget()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null || this.plugin.getGameManager().getGameState() != GameState.IN_GAME || event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ())
            return;

        float yaw = event.getTo().getYaw();
        float pitch = event.getTo().getPitch();

        if (yaw < this.launchLocation.getYaw() - 50 || yaw > this.launchLocation.getYaw() + 50)
            event.getTo().setYaw(this.launchLocation.getYaw());

        if (pitch > 20 || pitch < -60)
            event.getTo().setPitch(0);
    }
}
