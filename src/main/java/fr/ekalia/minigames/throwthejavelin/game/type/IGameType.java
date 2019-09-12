package fr.ekalia.minigames.throwthejavelin.game.type;

import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import java.util.List;
import org.bukkit.entity.Trident;

/**
 * @author roro1506_HD
 */
public interface IGameType {

    /**
     * Called when the game starts
     */
    void start();

    /**
     * Called when a new round begins
     */
    int nextRound();

    /**
     * Called when the game ends
     *
     * @param winners The three winners
     */
    void finish(List<GamePlayer> winners);

    /**
     * Called when the current player changes
     *
     * @param player The new shooting player
     */
    void nextPlayer(GamePlayer player);

    /**
     * Called to disable this game type
     */
    void disable();

    /**
     * Called when the {@link Trident} (Aka. Javelin) lands on the ground
     *
     * @param trident The trident
     * @param thrower The player who thrown the trident
     */
    void onJavelinLand(Trident trident, GamePlayer thrower);

}
