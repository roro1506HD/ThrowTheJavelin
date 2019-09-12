package fr.ekalia.minigames.throwthejavelin.map.color.converter;

/**
 * @author roro1506_HD
 */
public interface IColorConverter {

    byte convert(int color);

    byte convertBytes(byte[] buffer, int index);

}
