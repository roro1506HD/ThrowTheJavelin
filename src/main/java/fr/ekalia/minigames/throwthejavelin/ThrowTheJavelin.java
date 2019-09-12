package fr.ekalia.minigames.throwthejavelin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.ekalia.minigames.throwthejavelin.command.StartCommand;
import fr.ekalia.minigames.throwthejavelin.config.JavelinConfig;
import fr.ekalia.minigames.throwthejavelin.game.GameManager;
import fr.ekalia.minigames.throwthejavelin.listener.ItemListener;
import fr.ekalia.minigames.throwthejavelin.listener.PlayerListener;
import fr.ekalia.minigames.throwthejavelin.listener.ProjectileListener;
import fr.ekalia.minigames.throwthejavelin.listener.WorldListener;
import fr.ekalia.minigames.throwthejavelin.map.MapManager;
import fr.ekalia.minigames.throwthejavelin.packet.PacketManager;
import fr.ekalia.minigames.throwthejavelin.scoreboard.ScoreboardManager;
import fr.ekalia.minigames.throwthejavelin.util.json.LocationTypeAdapter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author roro1506_HD
 */
public class ThrowTheJavelin extends JavaPlugin {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(Location.class, new LocationTypeAdapter(this))
            .create();

    private JavelinConfig     config;
    private GameManager       gameManager;
    private PacketManager     packetManager;
    private ScoreboardManager scoreboardManager;
    private MapManager        mapManager;

    @Override
    public void onDisable() {
        this.mapManager.disable();
        this.gameManager.disable();
    }

    @Override
    public void onEnable() {
        // Register commands
        CommandMap commandMap = ((CraftServer) super.getServer()).getCommandMap();
        String fallback = super.getDescription().getName();

        commandMap.register(fallback, new StartCommand(this));

        // Load config, and create it if not found
        try {
            File configFile = new File(super.getDataFolder(), "config.json");
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            }

            try (FileReader reader = new FileReader(configFile)) {
                this.config = this.gson.fromJson(reader, JavelinConfig.class);
            }

            if (this.config == null) {
                this.config = new JavelinConfig();

                try (FileWriter writer = new FileWriter(configFile)) {
                    this.gson.toJson(this.config, JavelinConfig.class, writer);
                }
            }
        } catch (IOException ex) {
            this.log(ex);
        }

        // Register listeners
        super.getServer().getPluginManager().registerEvents(new ProjectileListener(this), this);
        super.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        super.getServer().getPluginManager().registerEvents(new WorldListener(), this);
        super.getServer().getPluginManager().registerEvents(new ItemListener(this), this);

        // Create packet manager
        this.packetManager = new PacketManager(this);

        // Create scoreboard manager
        this.scoreboardManager = new ScoreboardManager(this);

        // Create map manager
        this.mapManager = new MapManager(this);

        // Instantiate the Game Manager
        this.gameManager = new GameManager(this);
    }

    /**
     * Logs an error to the plugin logger
     *
     * @param throwable the error to log
     */
    public void log(Throwable throwable) {
        // TODO: Better log handling with auto-upload to gitlab snippets and proper error UUID
        this.getLogger().log(Level.SEVERE, "Caught exception : " + throwable.getMessage(), throwable);
    }

    /**
     * Gets the specific {@link Gson} instance of this mini-game
     *
     * @return the {@link Gson} instance
     */
    public Gson getGson() {
        return this.gson;
    }

    /**
     * Gets the Game Config, which contains Lobby, Launch & Spectator locations.
     *
     * @return the game config
     * @see JavelinConfig
     */
    public JavelinConfig getGameConfig() {
        return this.config;
    }

    /**
     * Gets the {@link GameManager}, the heart of the mini-game. It is used to start and manage all aspects of the game.
     *
     * @return the {@link GameManager}
     */
    public GameManager getGameManager() {
        return this.gameManager;
    }

    /**
     * @return the {@link PacketManager}
     */
    public PacketManager getPacketManager() {
        return this.packetManager;
    }

    /**
     * @return the {@link ScoreboardManager}
     */
    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    /**
     * @return the {@link MapManager}
     */
    public MapManager getMapManager() {
        return this.mapManager;
    }
}
