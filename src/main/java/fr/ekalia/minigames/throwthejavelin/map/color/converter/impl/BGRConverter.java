package fr.ekalia.minigames.throwthejavelin.map.color.converter.impl;

import fr.ekalia.minigames.throwthejavelin.map.color.MapColorPalette;
import fr.ekalia.minigames.throwthejavelin.map.color.converter.IColorConverter;

/**
 * @author roro1506_HD
 */
public class BGRConverter implements IColorConverter {

    @Override
    public byte convert(int color) {
        return MapColorPalette.getColor(color, color >> 8, color >> 16);
    }

    @Override
    public byte convertBytes(byte[] buffer, int index) {
        return MapColorPalette.getColor(buffer[index + 2], buffer[index + 1], buffer[index]);
    }
}
