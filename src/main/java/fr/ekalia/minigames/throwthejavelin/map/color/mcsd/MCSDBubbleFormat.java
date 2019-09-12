package fr.ekalia.minigames.throwthejavelin.map.color.mcsd;

import fr.ekalia.minigames.throwthejavelin.map.color.MapColorSpaceData;
import fr.ekalia.minigames.throwthejavelin.map.color.bit.BitInputStream;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * Stores all map color space information in a highly compressed bubble format.
 * In this format it is assumed the color data is in cell shapes. It stores the cell
 * borders separate from the colors using the {@link MCSDWebbingCodec}. These cells
 * are then filled with colors to reproduce the original image.
 *
 * https://github.com/bergerhealer/BKCommonLib/blob/master/src/main/java/com/bergerkiller/bukkit/common/map/color/MCSDBubbleFormat.java
 */
public class MCSDBubbleFormat extends MapColorSpaceData {

    private final boolean[][] strands = new boolean[256][65536 /* 256 * 256 */];
    private final ObjectList<Bubble> bubbles = new ObjectArrayList<>();

    public void readFrom(InputStream inputStream) throws IOException {

        try (BitInputStream bitStream = new BitInputStream(new GZIPInputStream(inputStream))) {
            // Read all color RGB values
            for (int i = 0; i < 256; i++) {
                int r = bitStream.read();
                int g = bitStream.read();
                int b = bitStream.read();
                int a = bitStream.read();
                this.setColor((byte) i, new Color(r, g, b, a));
            }

            // Read all bubbles from the stream
            while (true) {
                byte color = (byte) bitStream.read();

                if (color == 0)
                    break;

                this.bubbles.add(new Bubble(bitStream.read(), bitStream.read(), bitStream.read(), bitStream.read(), color));
            }

            // Read bubble boundary information from the stream
            MCSDWebbingCodec codec = new MCSDWebbingCodec();

            for (int z = 0; z < 256; z++) {
                Arrays.fill(this.strands[z], false);
                codec.reset(this.strands[z], false);
                while (codec.readNext(bitStream))
                    ;
            }

            // Initialize the colors with the bubble colors
            this.initColors();

            // Read color correction data for pixels unset (value = 0)
            for (int i = 0; i < (1 << 24); i++) {
                if (this.get(i) != 0)
                    continue;

                if (bitStream.readBits(1) == 0)
                    this.set(i, this.get(i - 1));
                else {
                    int mode = bitStream.readBits(2);
                    if (mode == 0b00)
                        this.set(i, this.get(i - 256));
                    else if (mode == 0b01)
                        this.set(i, this.get(i + 1));
                    else if (mode == 0b10)
                        this.set(i, this.get(i + 256));
                    else
                        this.set(i, (byte) bitStream.read());
                }
            }
        }
    }

    private void initColors() {
        // Set initial cell colors
        this.clearRGBData();

        for (Bubble bubble : this.bubbles)
            for (int z = bubble.getMinZ(); z <= bubble.getMaxZ(); z++)
                this.set(bubble.getX(), bubble.getY(), z, bubble.getColor());

        spreadColors();
    }

    private void spreadColors() {
        boolean[] all_strands = new boolean[1 << 24];

        for (int z = 0; z < 256; z++)
            System.arraycopy(this.strands[z], 0, all_strands, z << 16, 1 << 16);

        boolean mode = false;
        boolean hasChanges;

        do {
            hasChanges = false;

            // Alternate the direction in which we process every step
            // This prevents really slow filling when the direction is 'wrong'
            // The below logic is partially based on the light fixing algorithm in Light Cleaner
            int endIndex;
            int deltaIndex;
            int index;
            byte color;

            if (mode = !mode) {
                deltaIndex = 1;
                index = 0;
                endIndex = (1 << 24);
            } else {
                deltaIndex = -1;
                index = (1 << 24) - 1;
                endIndex = 0;
            }

            do {
                if (!all_strands[index]) {
                    all_strands[index] = true;

                    if ((index & 0xFF) < 0xFF) {
                        if ((color = this.get(index + 1)) != 0) {
                            this.set(index, color);
                            hasChanges = true;
                        } else if ((color = this.get(index)) != 0) {
                            this.set(index + 1, color);
                            hasChanges = true;
                        } else
                            all_strands[index] = false; // retry
                    }

                    if ((index & 0xFF00) < 0xFF00) {
                        if ((color = this.get(index + 256)) != 0) {
                            this.set(index, color);
                            hasChanges = true;
                        } else if ((color = this.get(index)) != 0) {
                            this.set(index + 256, color);
                            hasChanges = true;
                        } else
                            all_strands[index] = false; // retry
                    }
                }
            } while ((index += deltaIndex) != endIndex);
        } while (hasChanges);
    }

    public static class Bubble {

        private int x;
        private int y;
        private int minZ;
        private int maxZ;
        private byte color;

        Bubble(int x, int y, int minZ, int maxZ, byte color) {
            this.x = x;
            this.y = y;
            this.minZ = minZ;
            this.maxZ = minZ + maxZ;
            this.color = color;
        }

        public int getX() {
            return this.x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return this.y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getMinZ() {
            return this.minZ;
        }

        public void setMinZ(int minZ) {
            this.minZ = minZ;
        }

        public int getMaxZ() {
            return this.maxZ;
        }

        public void setMaxZ(int maxZ) {
            this.maxZ = maxZ;
        }

        public byte getColor() {
            return this.color;
        }

        public void setColor(byte color) {
            this.color = color;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.x, this.y, this.minZ, this.maxZ, this.color);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;

            if (!(o instanceof Bubble))
                return false;

            Bubble bubble = (Bubble) o;
            return this.x == bubble.x &&
                    this.y == bubble.y &&
                    this.minZ == bubble.minZ &&
                    this.maxZ == bubble.maxZ &&
                    this.color == bubble.color;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Bubble.class.getSimpleName() + "[", "]")
                    .add("x=" + this.x)
                    .add("y=" + this.y)
                    .add("zMin=" + this.minZ)
                    .add("zMax=" + this.maxZ)
                    .add("color=" + this.color)
                    .toString();
        }
    }
}
