package fr.ekalia.minigames.throwthejavelin.util;

import fr.ekalia.minigames.throwthejavelin.map.MapManager;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * @author roro1506_HD
 */
public class FontUtil {

    public static final Font OPEN_SANS;

    static {
        Font font = null;
        try {
            GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            font = Font.createFont(Font.TRUETYPE_FONT, MapManager.class.getResourceAsStream("/fr/ekalia/minigames/throwthejavelin/map/resources/OpenSans-Bold.ttf"));

            environment.registerFont(font);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        OPEN_SANS = font;
    }
}
