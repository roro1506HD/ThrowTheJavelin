package fr.ekalia.minigames.throwthejavelin.util.structure;

import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author roro1506_HD
 */
public class PatternStructureBuilder {

    private final ThrowTheJavelin plugin;
    private final String[] pattern;
    private final Object2ObjectMap<Character, String> palette;

    PatternStructureBuilder(ThrowTheJavelin plugin, String[] pattern) {
        this.plugin = plugin;
        this.pattern = pattern;
        this.palette = new Object2ObjectOpenHashMap<>();
    }

    /**
     * Adds an ingredient to the provided structure
     *
     * @param c The character which represent this palette in the pattern
     * @param blockPalette The palette
     * @return this builder
     */
    public PatternStructureBuilder ingredient(char c, String blockPalette) {
        if (this.palette.put(c, blockPalette) != null)
            throw new IllegalArgumentException("This ingredient is already registered!");

        return this;
    }

    /**
     * @return The {@link PatternStructure} instance of this builder
     */
    public PatternStructure build() {
        return new PatternStructure(this.plugin, this.pattern, this.palette);
    }
}
