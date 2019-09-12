package fr.ekalia.minigames.throwthejavelin.map.color.mcsd;

import fr.ekalia.minigames.throwthejavelin.map.color.bit.BitInputStream;
import fr.ekalia.minigames.throwthejavelin.map.color.bit.BitPacket;
import java.io.IOException;
import java.util.Arrays;

/**
 * Encodes or decodes a 256x256 grid of booleans by walking down the connected lines and encoding them
 * using drawing instructions. For example, a diagonal line in the grid may be encoded as follows:
 * <ul>
 * <li>SET_POSITION(23, 56)</li>
 * <li>SET_DX(-1)</li>
 * <li>SET_DY(1)</li>
 * <li>MOVE DX AND DRAW</li>
 * <li>MOVE DX AND DRAW</li>
 * <li>MOVE DY AND DRAW</li>
 * <li>MOVE DX AND DRAW</li>
 * <li>MOVE DX AND DRAW</li>
 * <li>MOVE DY AND DRAW</li>
 * <li>etc.</li>
 * </ul>
 * <p>
 * For encoding the data, the follow bits are written out in sequence:
 * <ul>
 * <li>00 -> MOVE DX AND DRAW</li>
 * <li>01 -> MOVE DY AND DRAW</li>
 * <li>10 -> MOVE DX+DY AND DRAW</li>
 * <li>11 100 -> SET DX = -1</li>
 * <li>11 101 -> SET DX = 1</li>
 * <li>11 110 -> SET DY = -1</li>
 * <li>11 111 -> SET DY = 1</li>
 * <li>11 00 [byte_x][byte_y] -> SET POSITION AND DRAW</li>
 * <li>11 01 -> STOP</li>
 * </ul>
 * <p>
 * https://github.com/bergerhealer/BKCommonLib/blob/master/src/main/java/com/bergerkiller/bukkit/common/map/color/MCSDWebbingCodec.java
 */
public class MCSDWebbingCodec {

    private boolean[] strands = new boolean[1 << 16];
    private int writtenCells;
    private int lastX;
    private int lastY;
    private int lastDX;
    private int lastDY;
    private int packetsCount = 0;
    private BitPacket[] packets = new BitPacket[1024];

    public MCSDWebbingCodec() {
        Arrays.fill(this.packets, new BitPacket());
    }

    public void reset(boolean[] cells, boolean copyCells) {
        if (copyCells)
            System.arraycopy(cells, 0, this.strands, 0, cells.length);
        else
            this.strands = cells;

        this.writtenCells = 0;
        this.lastX = -1000;
        this.lastY = -1000;
        this.lastDX = 1;
        this.lastDY = 1;
        this.packetsCount = 1;
    }

    public boolean readNext(BitInputStream inputStream) throws IOException {
        int op = inputStream.readBits(2);

        if (op == 0b11) {
            if (inputStream.readBits(1) == 1) {
                // Set DX/DY increment/decrement
                int sub = inputStream.readBits(2);

                if (sub == 0b00)
                    this.lastDX = -1;
                else if (sub == 0b01)
                    this.lastDX = 1;
                else if (sub == 0b10)
                    this.lastDY = -1;
                else
                    this.lastDY = 1;
            } else {
                // Command codes
                if (inputStream.readBits(1) == 1)
                    // End of slice
                    return false;

                this.lastX = inputStream.readBits(8);
                this.lastY = inputStream.readBits(8);
                this.strands[this.lastX | (this.lastY << 8)] = true;
            }
        } else {
            // Write next pixel
            if (op == 0b00)
                this.lastX += this.lastDX;
            else if (op == 0b01)
                this.lastY += this.lastDY;
            else if (op == 0b10) {
                this.lastX += this.lastDX;
                this.lastY += this.lastDY;
            }

            this.strands[this.lastX | (this.lastY << 8)] = true;
        }

        return true;
    }

    public boolean[] getStrands() {
        return this.strands;
    }
}
