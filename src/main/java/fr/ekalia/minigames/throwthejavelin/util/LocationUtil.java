package fr.ekalia.minigames.throwthejavelin.util;

import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.Vec3D;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 * @author roro1506_HD
 */
public class LocationUtil {

    private static final float PI_180 = (float) (Math.PI / 180.0F);
    private static final BlockFace[] AXIS = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};

    /**
     * This method is not from me, it's from Minecraft itself, if you need further documentation of how this works, see the relative teleport arguments :
     *
     * <code>/tp ^x ^y ^z</code>
     *
     * @param location the source location
     * @param x the x offset
     * @param y the y offset
     * @param z the z offset
     * @return the location after the calculation
     */
    public static Location getLocation(Location location, double x, double y, double z) {
        float f = MathHelper.cos((location.getYaw() + 90.0F) * PI_180);
        float f1 = MathHelper.sin((location.getYaw() + 90.0F) * PI_180);
        float f2 = MathHelper.cos(-location.getPitch() * PI_180);
        float f3 = MathHelper.sin(-location.getPitch() * PI_180);
        float f4 = MathHelper.cos((-location.getPitch() + 90.0F) * PI_180);
        float f5 = MathHelper.sin((-location.getPitch() + 90.0F) * PI_180);
        Vec3D vec3D1 = new Vec3D(f * f2, f3, f1 * f2);
        Vec3D vec3D2 = new Vec3D(f * f4, f5, f1 * f4);
        Vec3D vec3D3 = vec3D1.c(vec3D2).a(-1.0D);
        double d0 = vec3D1.x * z + vec3D2.x * y + vec3D3.x * x;
        double d1 = vec3D1.y * z + vec3D2.y * y + vec3D3.y * x;
        double d2 = vec3D1.z * z + vec3D2.z * y + vec3D3.z * x;
        return new Location(location.getWorld(), location.getX() + d0, location.getY() + d1, location.getZ() + d2, location.getYaw(), location.getPitch());
    }

    /**
     * Returns the {@link BlockFace} direction of the location's yaw
     *
     * @param location the location to get the yaw of
     * @return the direction
     */
    public static BlockFace getDirection(Location location) {
        return AXIS[Math.round(location.getYaw() / 90.0F) & 0x3];
    }
}
