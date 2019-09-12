package fr.ekalia.minigames.throwthejavelin.listener;

import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import fr.ekalia.minigames.throwthejavelin.game.GameState;
import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

/**
 * @author roro1506_HD
 */
public class ProjectileListener implements Listener {

    private final ThrowTheJavelin plugin;

    public ProjectileListener(ThrowTheJavelin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Trident) || !(event.getEntity().getShooter() instanceof Player) || this.plugin.getGameManager().getGameState() != GameState.IN_GAME)
            return;

        GamePlayer player = this.plugin.getGameManager().getPlayer(((Player) event.getEntity().getShooter()).getUniqueId());
        Trident trident = (Trident) event.getEntity();

        trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

        this.plugin.getGameManager().setCurrentJavelin(trident);

        player.sendMountPacket(this.plugin.getGameManager().getLaunchEntityId(), false);
        player.getPlayer().getInventory().clear();
        player.getNPC().spawn(player.getPlayer().getLocation());

        Location specLocation = this.plugin.getGameConfig().getSpecLocation();
        ArmorStand specCorpse = this.plugin.getGameManager().getSpectatorCorpse();

        specCorpse.setGravity(true);
        for (GamePlayer gamePlayer : this.plugin.getGameManager().getPlayers()) {
            gamePlayer.getPlayer().setGameMode(GameMode.SPECTATOR);
            gamePlayer.getPlayer().getHandle().setInvisible(false);
            gamePlayer.getPlayer().teleport(specLocation);
            gamePlayer.getPlayer().setSpectatorTarget(specCorpse);
        }
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident) || !(event.getEntity().getShooter() instanceof Player) || this.plugin.getGameManager().getGameState() != GameState.IN_GAME)
            return;

        this.plugin.getGameManager().onJavelinLand((Trident) event.getEntity(), this.plugin.getGameManager().getPlayer(((Player) event.getEntity().getShooter()).getUniqueId()));
    }
}
