package fr.ekalia.minigames.throwthejavelin.map;

import fr.ekalia.minigames.throwthejavelin.ThrowTheJavelin;
import fr.ekalia.minigames.throwthejavelin.game.player.GamePlayer;
import fr.ekalia.minigames.throwthejavelin.map.color.converter.ImageConverter;
import fr.ekalia.minigames.throwthejavelin.util.LocationUtil;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

/**
 * @author roro1506_HD
 */
class MapWall {

    private static final AtomicInteger EXECUTOR_COUNT = new AtomicInteger(0);
    private static final Executor EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), runnable -> new Thread(runnable, "MapWall Thread #" + EXECUTOR_COUNT.incrementAndGet()));

    private static int ID = 1;

    private final ThrowTheJavelin plugin;
    private final Location location;
    private final IMapRenderer renderer;
    private final int columns;
    private final int rows;
    private final ItemFrame[][] frames;
    private final Object2ObjectMap<UUID, byte[][]> imageCache;

    MapWall(ThrowTheJavelin plugin, Location location, int columns, int rows, IMapRenderer renderer) {
        this.plugin = plugin;
        this.location = location;
        this.renderer = renderer;
        this.columns = columns;
        this.rows = rows;
        this.frames = new ItemFrame[columns][rows];
        this.imageCache = new Object2ObjectOpenHashMap<>();

        for (int column = 0; column < columns; column++)
            for (int row = 0; row < rows; row++) {
                ItemFrame frame = location.getWorld().spawn(LocationUtil.getLocation(this.location.clone(), column, -row, 0), ItemFrame.class);
                ItemStack map = new ItemStack(Material.FILLED_MAP, 1);
                MapMeta meta = (MapMeta) map.getItemMeta();

                meta.setMapView(new EmptyMapView(ID++));
                map.setItemMeta(meta);

                frame.setFacingDirection(LocationUtil.getDirection(location));
                frame.setItem(map);

                this.frames[column][row] = frame;
            }
    }

    void disable() {
        Arrays.stream(this.frames)
                .flatMap(Arrays::stream)
                .forEach(Entity::remove);
    }

    public boolean containsMap(int mapId) {
        return Arrays.stream(this.frames)
                .flatMap(Arrays::stream)
                .map(ItemFrame::getItem)
                .map(ItemStack::getItemMeta)
                .map(MapMeta.class::cast)
                .filter(Objects::nonNull)
                .map(MapMeta::getMapView)
                .filter(Objects::nonNull)
                .map(MapView::getId)
                .anyMatch(id -> id == mapId);
    }

    private void update(GamePlayer player) {
        BufferedImage bufferedImage = new BufferedImage(this.columns << 7, this.rows << 7, BufferedImage.TYPE_INT_ARGB);

        try {
            this.renderer.render(bufferedImage, player);
        } catch (Exception ex) {
            this.plugin.log(ex);
        }

        byte[][] imageDataMatrix = new byte[256][16384];

        for (int column = 0; column < this.columns; column++)
            for (int row = 0; row < this.rows; row++) {
                BufferedImage chunk = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = chunk.createGraphics();

                graphics.drawImage(bufferedImage, 0, 0, 128, 128, 128 * column, 128 * row, 128 * column + 128, 128 * row + 128, null);
                graphics.dispose();

                imageDataMatrix[((MapMeta) this.frames[column][row].getItem().getItemMeta()).getMapView().getId()] = ImageConverter.imageToBytes(chunk);
            }

        this.imageCache.put(player.getUuid(), imageDataMatrix); // Cache the matrix, to prevent all redundant calculations
    }

    CompletableFuture<byte[]> renderFrame(int mapId, GamePlayer player) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();

        EXECUTOR.execute(() ->
        {
            if (!this.imageCache.containsKey(player.getUuid()))
                this.update(player);

            future.complete(this.imageCache.get(player.getUuid())[mapId]);
        });

        return future;
    }
}
