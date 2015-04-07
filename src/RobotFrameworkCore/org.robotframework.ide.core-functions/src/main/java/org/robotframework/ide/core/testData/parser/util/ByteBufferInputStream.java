package org.robotframework.ide.core.testData.parser.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import org.robotframework.ide.core.testData.parser.IParsePositionMarkable;


public class ByteBufferInputStream extends InputStream implements
        IParsePositionMarkable {

    private final AtomicInteger readBytes;
    private final AtomicInteger readLimit;

    private final ByteBuffer buffer;


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

        return buffer.get() & 0xFF;
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

        return length;
    }


    @Override
    public void mark() {

    }


    @Override
    public void mark(int readLimit) {

    }


    @Override
    public void reset() {

    }
}
