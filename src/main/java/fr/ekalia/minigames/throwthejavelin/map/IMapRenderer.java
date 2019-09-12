package fr.ekalia.minigames.throwthejavelin.map;

import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import java.awt.image.BufferedImage;

/**
 * @author roro1506_HD
 */
@FunctionalInterface
public interface IMapRenderer {

    void render(BufferedImage image, GamePlayer player);

}
