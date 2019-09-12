package fr.ekalia.minigames.throwthejavelin.map.color.converter;

import fr.ekalia.minigames.throwthejavelin.map.color.MapColorPalette;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImageConverter {

    /**
     * @see org.bukkit.map.MapPalette#imageToBytes(Image)
     */
    public static byte[] imageToBytes(Image image) {
        BufferedImage tempImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = tempImage.createGraphics();

        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        int[] pixels = new int[tempImage.getWidth() * tempImage.getHeight()];
        tempImage.getRGB(0, 0, tempImage.getWidth(), tempImage.getHeight(), pixels, 0, tempImage.getWidth());

        byte[] result = new byte[pixels.length];
        for (int i = 0; i < pixels.length; i++)
            result[i] = MapColorPalette.getColor(new Color(pixels[i], true));

        return result;
    }
}
