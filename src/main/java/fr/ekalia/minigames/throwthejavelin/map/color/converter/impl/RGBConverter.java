package fr.ekalia.minigames.throwthejavelin.map.color.converter.impl;

import fr.ekalia.minigames.throwthejavelin.map.color.MapColorPalette;
import fr.ekalia.minigames.throwthejavelin.map.color.converter.IColorConverter;

/**
 * @author roro1506_HD
 */
public class RGBConverter implements IColorConverter {

    @Override
    public byte convert(int color) {
        return MapColorPalette.getColor(color >> 16, color >> 8, color);
    }

    @Override
    public byte convertBytes(byte[] buffer, int index) {
        return MapColorPalette.getColor(buffer[index], buffer[index + 1], buffer[index + 2]);
    }
}
