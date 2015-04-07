package org.robotframework.ide.core.testData.parser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


/**
 * Decorator for {@link ByteBuffer} for give possibility to read by
 * {@link BufferedReader}
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class ByteBufferInputStream extends InputStream {

    private final ByteBuffer buffer;


    /**
     * @param buffer
     *            wrapped to input stream {@link ByteBuffer}
     */
    public ByteBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }


    @Override
    public synchronized int read() throws IOException {
        if (!buffer.hasRemaining()) {
            return -1;
        }
        return buffer.get() & 0xFF;
    }


    @Override
    public int available() throws IOException {
        return buffer.remaining();
    }


    /**
     * {@link ByteBuffer#mark()}
     */
    public void mark() {
        buffer.mark();
    }


    /**
     * {@link ByteBuffer#reset()}
     */
    @Override
    public void reset() {
        buffer.reset();
    }


    @Override
    public synchronized int read(byte[] bytes, int offset, int length)
            throws IOException {
        if (!buffer.hasRemaining()) {
            return -1;
        }

        length = Math.min(length, buffer.remaining());
        buffer.get(bytes, offset, length);
        return length;
    }
}
