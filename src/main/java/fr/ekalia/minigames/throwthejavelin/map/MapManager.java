package fr.ekalia.minigames.throwthejavelin.map;

import com.google.common.base.Preconditions;
import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import java.util.ArrayList;
import net.minecraft.server.v1_14_R1.PacketPlayOutMap;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * @author roro1506_HD
 */
public class MapManager {

    private final ThrowTheJavelin plugin;
    private final ObjectList<MapWall> walls;

    public MapManager(ThrowTheJavelin plugin) {
        this.plugin = plugin;
        this.walls = new ObjectArrayList<>();

        plugin.getPacketManager().addHandler(PacketPlayOutMap.class, event ->
        {
            int mapId = event.getField(Integer.class, "a");

            if (mapId < 0) // It is our packet
            {
                event.setField("a", -mapId); // Set the correct id
                return; // Stop here, we don't want an infinite loop
            }

            // Retrieve the MapWall from the id
            MapWall mapWall = this.fromMapId(mapId);

            if (mapWall == null)
                return;

            event.setCancelled(true);

            // When the frame is rendered, send it to the player, with the reversed id to ensure we send it
            mapWall.renderFrame(mapId, event.getPlayer()).thenAccept(data -> event.getPlayer().sendPacket(new PacketPlayOutMap(-mapId, (byte) 4, false, true, new ArrayList<>(), data, 0, 0, 128, 128)));
        });
    }

    /**
     * Disables this module
     */
    public void disable() {
        this.walls.forEach(MapWall::disable);
    }

    /**
     * Creates a map wall at the provided location
     *
     * @param location The top-left corner of this map wall
     * @param columns The number of columns this wall will be long of
     * @param rows The amount of rows this wall will have
     * @param renderer The renderer of this wall
     */
    public void createMapWall(Location location, int columns, int rows, IMapRenderer renderer) {
        Preconditions.checkNotNull(location.getWorld());
        this.walls.add(new MapWall(this.plugin, location, columns, rows, renderer));
    }

    /**
     * Gets the {@link MapWall} from a map id
     *
     * @param mapId the map id
     * @return the {@link MapWall} of the map id, {@code null} if not found
     */
    private MapWall fromMapId(int mapId) {
        return this.walls.stream()
                .filter(mapWall -> mapWall.containsMap(mapId))
                .findFirst()
                .orElse(null);
    }
}
