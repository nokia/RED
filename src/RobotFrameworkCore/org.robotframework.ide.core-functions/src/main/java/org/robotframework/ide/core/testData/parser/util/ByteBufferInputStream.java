package org.robotframework.ide.core.testData.parser.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import org.robotframework.ide.core.testData.parser.IParsePositionMarkable;


/**
 * Take {@link ByteBuffer} as data provider and read byte-by-byte with
 * possibility to pushback read content.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class ByteBufferInputStream extends InputStream implements
        IParsePositionMarkable {

    private final AtomicInteger readBytes;
    private final AtomicInteger readLimit;

    private final ByteBuffer buffer;


    /**
     * 
     * @param buffer
     */
    public ByteBufferInputStream(final ByteBuffer buffer) {
        this.buffer = buffer;
        this.readBytes = new AtomicInteger(0);
        this.readLimit = new AtomicInteger(-1);
    }


    @Override
    public int read() throws IOException {
        if (!buffer.hasRemaining()) {
            return -1;
        }

        this.readBytes.incrementAndGet();

        return buffer.get() & 0xFF;
    }


    /**
     * 
     * @return current unread byte
     * @throws IOException
     */
    public int currentByteInBuffer() throws IOException {
        int c = -1;
        if (buffer.hasRemaining()) {
            c = buffer.get(buffer.position());
        }

        return c;
    }


    @Override
    public int available() throws IOException {
        return buffer.remaining();
    }


    @Override
    public synchronized int read(byte[] bytes, int off, int len)
            throws IOException {
        int length = -1;

        if (buffer.hasRemaining()) {
            length = Math.min(len, buffer.remaining());
            buffer.get(bytes, off, length);
        }

        if (length > 0) {
            this.readBytes.addAndGet(length);
        }

        return length;
    }


    @Override
    public void mark() {
        buffer.mark();
        this.readLimit.set(-1);
        this.readBytes.set(0);
    }


    @Override
    public void mark(int readLimit) {
        if (readLimit < 0) {
            throw new IllegalArgumentException(
                    "Got read limit for buffer less than zero: " + readLimit);
        }

        buffer.mark();
        this.readLimit.set(readLimit);
        this.readBytes.set(0);
    }


    @Override
    public void reset() {
        if (readLimit.get() == -1 || readLimit.get() >= readBytes.get()) {
            buffer.reset();
        }
    }
}
