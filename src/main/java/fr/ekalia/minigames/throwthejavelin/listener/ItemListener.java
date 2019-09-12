package fr.ekalia.minigames.throwthejavelin.listener;

import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author roro1506_HD
 */
public class ItemListener implements Listener {

    public static final ItemStack PLAYER_SHOW;
    private static final ItemStack PLAYER_HIDE;

    private final ThrowTheJavelin plugin;

    public ItemListener(ThrowTheJavelin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (PLAYER_HIDE.equals(event.getItem())) {
            for (GamePlayer player : this.plugin.getGameManager().getPlayers())
                if (!event.getPlayer().getUniqueId().equals(player.getUuid()))
                    event.getPlayer().hidePlayer(this.plugin, player.getPlayer());

            event.getPlayer().getInventory().setItemInMainHand(PLAYER_SHOW);
        } else if (PLAYER_SHOW.equals(event.getItem())) {
            for (GamePlayer player : this.plugin.getGameManager().getPlayers())
                if (!event.getPlayer().getUniqueId().equals(player.getUuid()))
                    event.getPlayer().showPlayer(this.plugin, player.getPlayer());

            event.getPlayer().getInventory().setItemInMainHand(PLAYER_HIDE);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    static {
        // Ouais c'est pas ouf, mais y'a pas plus d'items spéciaux du coup flemme de faire toute une api

        PLAYER_HIDE = new ItemStack(Material.GRAY_DYE);
        PLAYER_SHOW = new ItemStack(Material.LIME_DYE);

        ItemMeta itemMeta = PLAYER_HIDE.getItemMeta();

        itemMeta.setDisplayName("§cCacher les joueurs");
        PLAYER_HIDE.setItemMeta(itemMeta);

        itemMeta = PLAYER_SHOW.getItemMeta();

        itemMeta.setDisplayName("§aAfficher les joueurs");
        PLAYER_SHOW.setItemMeta(itemMeta);
    }
}
