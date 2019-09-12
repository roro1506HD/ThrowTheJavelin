package fr.ekalia.minigames.throwthejavelin.map.color.bit;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input Stream that can also read individual bits
 * <p>
 * https://github.com/bergerhealer/BKCommonLib/blob/master/src/main/java/com/bergerkiller/bukkit/common/io/BitInputStream.java
 */
public class BitInputStream extends InputStream {

    private final InputStream inputStream;
    private final boolean closeInputStream;

    private int buffer = 0;
    private int bufferLength = 0;
    private boolean closed;

    /**
     * Initializes a new Bit Input Stream, reading from the Input Stream specified
     *
     * @param inputStream to read from
     */
    public BitInputStream(InputStream inputStream) {
        this(inputStream, true);
    }

    /**
     * Initializes a new Bit Input Stream, reading from the Input Stream specified
     *
     * @param inputStream to read from
     * @param closeInputStream whether to close the underlying input stream when closing this stream
     */
    public BitInputStream(InputStream inputStream, boolean closeInputStream) {
        this.inputStream = inputStream;
        this.closeInputStream = closeInputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        return this.readBits(8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException {
        if (this.closed)
            throw new IOException("Stream is closed");

        return this.inputStream.available();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (this.closed)
            return;

        this.closed = true;

        if (this.closeInputStream)
            this.inputStream.close();
    }

    /**
     * Reads bits from the stream
     *
     * @param nBits to read
     * @return read value, -1 when end of stream is reached
     * @throws IOException if an I/O error occurs.
     */
    public int readBits(int nBits) throws IOException {
        if (this.closed)
            throw new IOException("Stream is closed");

        while (this.bufferLength < nBits) {
            int readByte = -1;

            try {
                readByte = this.inputStream.read();
            } catch (IOException ignored) {
            }

            if (readByte == -1)
                return -1;

            this.buffer |= (readByte << this.bufferLength);
            this.bufferLength += 8;
        }

        int result = this.buffer & ((1 << nBits) - 1);

        this.buffer >>= nBits;
        this.bufferLength -= nBits;

        return result;
    }
}
