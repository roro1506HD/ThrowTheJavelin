package fr.ekalia.minigames.throwthejavelin.util.json;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author roro1506_HD
 */
public class LocationTypeAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    /**
     * Plugin instance used to get {@link org.bukkit.World} from its name
     */
    private final JavaPlugin plugin;

    public LocationTypeAdapter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Location deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (!element.isJsonObject())
            return null;

        JsonObject location = element.getAsJsonObject();

        return new Location(
                this.plugin.getServer().getWorld(location.get("world").getAsString()),
                location.get("x").getAsDouble(),
                location.get("y").getAsDouble(),
                location.get("z").getAsDouble(),
                location.get("yaw").getAsFloat(),
                location.get("pitch").getAsFloat()
        );
    }

    @Override
    public JsonElement serialize(Location location, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        object.add("world", new JsonPrimitive(Optional.ofNullable(location.getWorld()).map(World::getName).orElse("null")));
        object.add("x", new JsonPrimitive(location.getX()));
        object.add("y", new JsonPrimitive(location.getY()));
        object.add("z", new JsonPrimitive(location.getZ()));
        object.add("yaw", new JsonPrimitive(location.getYaw()));
        object.add("pitch", new JsonPrimitive(location.getPitch()));

        return object;
    }
}
