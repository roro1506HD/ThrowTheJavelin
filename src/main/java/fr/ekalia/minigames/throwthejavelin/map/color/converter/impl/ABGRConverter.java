package fr.ekalia.minigames.throwthejavelin.map.color.converter.impl;

import fr.ekalia.minigames.throwthejavelin.map.color.MapColorPalette;
import fr.ekalia.minigames.throwthejavelin.map.color.converter.IColorConverter;

/**
 * @author roro1506_HD
 */
public class ABGRConverter implements IColorConverter {

    @Override
    public byte convert(int color) {
        if ((color & 0x80000000) == 0)
            return MapColorPalette.COLOR_TRANSPARENT;

        return MapColorPalette.getColor(color, color >> 8, color >> 16);
    }

    @Override
    public byte convertBytes(byte[] buffer, int index) {
        if ((buffer[index] & 0x80) == 0)
            return MapColorPalette.COLOR_TRANSPARENT;

        return MapColorPalette.getColor(buffer[index + 3], buffer[index + 2], buffer[index + 1]);
    }
}
