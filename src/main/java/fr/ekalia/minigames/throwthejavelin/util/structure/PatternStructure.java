package fr.ekalia.minigames.throwthejavelin.util.structure;

import com.mojang.brigadier.StringReader;
import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import java.util.Map;
import net.minecraft.server.v1_14_R1.ArgumentTile;
import net.minecraft.server.v1_14_R1.ArgumentTileLocation;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.TileEntity;
import net.minecraft.server.v1_14_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;

/**
 * @author roro1506_HD
 */
public class PatternStructure {

    private final ThrowTheJavelin plugin;
    private final Object2ObjectMap<BlockPosition, ArgumentTileLocation> palette;

    private Object2ObjectMap<BlockPosition, ArgumentTileLocation> placementBackup;
    private WorldServer pastedWorld;

    PatternStructure(ThrowTheJavelin plugin, String[] pattern, Map<Character, String> palette) {
        this.plugin = plugin;
        this.palette = new Object2ObjectOpenHashMap<>();

        int lastLength = -1;
        for (String patternEntry : pattern) {
            if (lastLength != -1 && patternEntry.length() != lastLength)
                throw new IllegalArgumentException("Pattern length is not same for all");

            lastLength = patternEntry.length();
        }

        try {
            int xMin = -(pattern.length / 2);
            int zMin = -(pattern[0].length() / 2);
            ArgumentTile argumentParser = new ArgumentTile();

            char c;
            for (int x = 0; x < pattern.length; x++)
                for (int z = 0; z < pattern[x].length(); z++)
                    if ((c = pattern[x].charAt(z)) != ' ')
                        this.palette.put(new BlockPosition(xMin + x, 0, zMin + z), argumentParser.parse(new StringReader(palette.get(c))));
        } catch (Exception ex) {
            plugin.log(ex);
        }
    }

    /**
     * Creates a builder of {@link PatternStructure}
     *
     * @param pattern the pattern of the structure
     * @return the structure builder
     */
    public static PatternStructureBuilder builder(ThrowTheJavelin plugin, String... pattern) {
        return new PatternStructureBuilder(plugin, pattern);
    }

    /**
     * Pastes this structure at the provided location
     *
     * @param location The location to paste the structure to
     * @throws IllegalStateException if this pattern has already been pasted
     * @throws IllegalArgumentException if the provided location world is null
     */
    public void paste(Location location) {
        if (this.placementBackup != null)
            throw new IllegalStateException("This pattern has already been pasted, undo it if you want to paste it again");

        if (location.getWorld() == null)
            throw new IllegalArgumentException("Location's world cannot be null");

        this.placementBackup = new Object2ObjectOpenHashMap<>();
        this.pastedWorld = ((CraftWorld) location.getWorld()).getHandle();

        this.palette.forEach((position, pattern) ->
        {
            try {
                BlockPosition pos = position.a(location.getX(), location.getY(), location.getZ());
                IBlockData blockData = this.pastedWorld.getType(pos);
                TileEntity tileEntity = this.pastedWorld.getTileEntity(pos);

                // Save old block data / nbt
                this.placementBackup.put(pos, new ArgumentTileLocation(blockData, blockData.getStateMap().keySet(), tileEntity == null ? null : tileEntity.b()));

                pattern.a(this.pastedWorld, pos, 2);
            } catch (Exception ex) {
                this.plugin.log(ex);
            }
        });
    }

    /**
     * Undo the previously pasted pattern
     *
     * @throws IllegalStateException if the pattern was not pasted
     */
    public void undo() {
        if (this.placementBackup == null)
            throw new IllegalStateException("This pattern is not pasted, you need to paste it before undoing it!");

        this.placementBackup.forEach((position, pattern) ->
        {
            try {
                pattern.a(this.pastedWorld, position, 2);
            } catch (Exception ex) {
                this.plugin.log(ex);
            }
        });

        this.placementBackup = null;
        this.pastedWorld = null;
    }

    /**
     * @return true if this structure has been pasted, otherwise false
     */
    public boolean isPasted() {
        return this.placementBackup != null;
    }
}
