package fr.ekalia.minigames.throwthejavelin.map;

import java.util.List;
import org.bukkit.World;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

/**
 * @author roro1506_HD
 */
class EmptyMapView implements MapView {

    private final int mapId;

    EmptyMapView(int mapId) {
        this.mapId = mapId;
    }

    @Override
    public int getId() {
        return this.mapId;
    }

    @Override
    public boolean isVirtual() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Scale getScale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setScale(Scale scale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCenterX() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCenterZ() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCenterZ(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public World getWorld() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWorld(World world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MapRenderer> getRenderers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addRenderer(MapRenderer mapRenderer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeRenderer(MapRenderer mapRenderer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTrackingPosition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTrackingPosition(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUnlimitedTracking() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUnlimitedTracking(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLocked() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocked(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCenterX(int i) {
        throw new UnsupportedOperationException();
    }
}
