package fr.ekalia.minigames.throwthejavelin.map.color;

import fr.ekalia.minigames.throwthejavelin.map.color.mcsd.MCSDBubbleFormat;
import java.awt.Color;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Additional functionality on top of Bukkit's MapPalette
 * <p>
 * https://github.com/bergerhealer/BKCommonLib/blob/master/src/main/java/com/bergerkiller/bukkit/common/map/MapColorPalette.java
 */
public class MapColorPalette {

    public static final byte[] COLOR_MAP_AVERAGE = new byte[0x10000];
    public static final byte[] COLOR_MAP_ADD = new byte[0x10000];
    public static final byte[] COLOR_MAP_SUBTRACT = new byte[0x10000];
    public static final byte[] COLOR_MAP_MULTIPLY = new byte[0x10000];
    public static final byte[] COLOR_MAP_SPECULAR = new byte[0x10000];

    // List of colors with their closest matching palette entry
    public static final byte COLOR_TRANSPARENT = 0;

    private static final MapColorSpaceData COLOR_MAP_DATA = new MapColorSpaceData();

    private static void initTables(byte color1, byte color2) {
        int index = getMapIndex(color1, color2);

        if (isTransparent(color1) || isTransparent(color2))
            initTransparent(index, color1, color2);
        else {
            Color c1 = getRealColor(color1);
            Color c2 = getRealColor(color2);

            initColor(index, c1.getRed(), c1.getGreen(), c1.getBlue(), c2.getRed(), c2.getGreen(), c2.getBlue());
        }
    }

    private static void initTransparent(int index, byte color1, byte color2) {
        COLOR_MAP_AVERAGE[index] = color2;
        COLOR_MAP_ADD[index] = color2;
        COLOR_MAP_SUBTRACT[index] = color2;
        COLOR_MAP_MULTIPLY[index] = 0;
    }

    private static void initColor(int index, int r1, int g1, int b1, int r2, int g2, int b2) {
        initArray(COLOR_MAP_AVERAGE, index, (r1 + r2) >> 1, (g1 + g2) >> 1, (b1 + b2) >> 1);
        initArray(COLOR_MAP_ADD, index, (r1 + r2), (g1 + g2), (b1 + b2));
        initArray(COLOR_MAP_SUBTRACT, index, (r2 - r1), (g2 - g1), (b2 - b1));
        initArray(COLOR_MAP_MULTIPLY, index, (r1 * r2) / 255, (g1 * g2) / 255, (b1 * b2) / 255);
    }

    private static void initArray(byte[] array, int index, int r, int g, int b) {
        if (r < 0x00)
            r = 0x00;
        else if (r > 0xFF)
            r = 0xFF;

        if (g < 0x00)
            g = 0x00;
        else if (g > 0xFF)
            g = 0xFF;

        if (b < 0x00)
            b = 0x00;
        else if (b > 0xFF)
            b = 0xFF;

        array[index] = getColor(r, g, b);
    }

    /**
     * Gets whether a particular color code is a transparent color.
     * There are 4 transparent colors available. Usually value 0 is used.
     *
     * @param color value
     * @return true if transparent
     */
    public static boolean isTransparent(byte color) {
        return (color & 0xFF) < 0x4;
    }

    /**
     * Gets the Minecraft map color code for an RGB color
     *
     * @param color input
     * @return minecraft color
     */
    public static byte getColor(Color color) {
        if ((color.getAlpha() & 0x80) == 0) {
            return COLOR_TRANSPARENT;
        } else {
            return COLOR_MAP_DATA.get(color.getRed(), color.getGreen(), color.getBlue());
        }
    }

    /**
     * Gets the Minecraft map color code for an RGB color
     *
     * @param r - red component
     * @param g - green component
     * @param b - blue component
     * @return minecraft color
     */
    public static byte getColor(byte r, byte g, byte b) {
        return COLOR_MAP_DATA.get(r, g, b);
    }

    /**
     * Gets the Minecraft map color code for an RGB color
     *
     * @param r - red component
     * @param g - green component
     * @param b - blue component
     * @return minecraft color
     */
    public static byte getColor(int r, int g, int b) {
        // This helps prevent dumb exceptions.
        // Nobody likes random exceptions when all you're doing is color calculations
        if (r < 0)
            r = 0;
        else if (r > 255)
            r = 255;

        if (g < 0)
            g = 0;
        else if (g > 255)
            g = 255;

        if (b < 0)
            b = 0;
        else if (b > 255)
            b = 255;

        return COLOR_MAP_DATA.get(r, g, b);
    }

    /**
     * Gets the index into one of the palette remap arrays
     *
     * @param color_a first color
     * @param color_b second color
     * @return index
     */
    public static int getMapIndex(byte color_a, byte color_b) {
        return (color_a & 0xFF) | ((color_b & 0xFF) << 8);
    }

    /**
     * Gets the real RGB color belonging to a color code
     *
     * @param color code input
     * @return real RGB color
     */
    public static Color getRealColor(byte color) {
        return COLOR_MAP_DATA.getColor(color);
    }

    /**
     * Gets the real RGB color belonging to a color code
     *
     * @param index of the color
     * @return real RGB color
     */
    public static Color getRealColor(int index) {
        return COLOR_MAP_DATA.getColor(index);
    }

    static {
        // Load color map data from the Bubble format file bundled with the library
        {
            MCSDBubbleFormat bubbleFormat = new MCSDBubbleFormat();

            try {
                InputStream inputStream = MapColorPalette.class.getResourceAsStream("/fr/ekalia/minigames/throwthejavelin/map/resources/map_1_12.bub");

                if (inputStream == null)
                    throw new IllegalStateException("Unable to find the required map bubble file!");

                bubbleFormat.readFrom(inputStream);
                COLOR_MAP_DATA.readFrom(bubbleFormat);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        // Generate 256 lightness values for all colors
        for (int a = 0; a < 256; a++) {
            int index = a * 256;
            Color color = getRealColor((byte) a);

            if (color.getAlpha() < 128)
                Arrays.fill(COLOR_MAP_SPECULAR, index, index + 256, COLOR_TRANSPARENT);
            else
                for (int b = 0; b < 256; b++) {
                    // 0.0 = black
                    // 1.0 = natural color
                    // 2.0 = white
                    float f = (float) b / 128.0F;
                    int sr = (int) (color.getRed() * f);
                    int sg = (int) (color.getGreen() * f);
                    int sb = (int) (color.getBlue() * f);
                    COLOR_MAP_SPECULAR[index++] = getColor(sr, sg, sb);
                }
        }

        // Initialize the color map tables for all possible color values
        for (int c1 = 0; c1 < 256; c1++)
            for (int c2 = 0; c2 < 256; c2++)
                initTables((byte) c1, (byte) c2);
    }
}
