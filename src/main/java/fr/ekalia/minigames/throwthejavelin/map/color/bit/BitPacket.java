package fr.ekalia.minigames.throwthejavelin.map.color.bit;

import java.util.Objects;

/**
 * Simple container for multiple bits of data.
 * <p>
 * https://github.com/bergerhealer/BKCommonLib/blob/master/src/main/java/com/bergerkiller/bukkit/common/io/BitPacket.java
 */
public class BitPacket implements Cloneable {

    public int data;
    public int bits;

    public BitPacket() {
        this.data = 0;
        this.bits = 0;
    }

    public BitPacket(int data, int bits) {
        this.data = data;
        this.bits = bits;
    }

    /**
     * Reads some bits from this packet, shifting the bits out of the buffer
     *
     * @param nBits to read
     * @return bit data
     */
    public int read(int nBits) {
        int result = data & ((1 << nBits) - 1);

        this.data >>= nBits;
        this.bits -= nBits;

        return result;
    }

    /**
     * Writes some bits to this packet, increasing the number of bits stored
     *
     * @param data to write
     * @param nBits of data
     */
    public void write(int data, int nBits) {
        this.data |= (data << this.bits);
        this.bits += nBits;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.data, this.bits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof BitPacket))
            return false;

        BitPacket bitPacket = (BitPacket) o;
        int mask = ((1 << this.bits) - 1);

        return this.bits == bitPacket.bits &&
                (this.data & mask) == (bitPacket.data & mask);
    }

    @Override
    public BitPacket clone() {
        return new BitPacket(this.data, this.bits);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(Integer.toBinaryString(this.data & ((1 << bits) - 1)));

        while (str.length() < this.bits)
            str.insert(0, "0");

        return str.toString();
    }
}