package fr.ekalia.minigames.throwthejavelin.map.color.converter;

import fr.ekalia.minigames.throwthejavelin.map.color.converter.impl.ABGRConverter;
import fr.ekalia.minigames.throwthejavelin.map.color.converter.impl.ARGBConverter;
import fr.ekalia.minigames.throwthejavelin.map.color.converter.impl.BGRConverter;
import fr.ekalia.minigames.throwthejavelin.map.color.converter.impl.RGBConverter;

/**
 * @author roro1506_HD
 */
class ColorConverterType {

    static final RGBConverter RGB = new RGBConverter();
    static final ARGBConverter ARGB = new ARGBConverter();
    static final BGRConverter BGR = new BGRConverter();
    static final ABGRConverter ABGR = new ABGRConverter();
}
