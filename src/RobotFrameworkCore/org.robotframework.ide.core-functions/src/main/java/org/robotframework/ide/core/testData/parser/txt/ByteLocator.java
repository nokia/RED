package org.robotframework.ide.core.testData.parser.txt;

import org.robotframework.ide.core.testData.parser.IDataLocator;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * Represents start and position in byte stream.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class ByteLocator implements IDataLocator<ByteBufferInputStream> {

    private final int begin;
    private final int end;
    private final ByteBufferInputStream byteStream;


    public ByteLocator(final ByteBufferInputStream byteStream, final int begin,
            final int end) {
        this.byteStream = byteStream;
        this.begin = begin;
        this.end = end;
    }


    @Override
    public void moveToStart() {
        byteStream.getByteBuffer().position(begin);
    }


    @Override
    public void moveToEnd() {
        byteStream.getByteBuffer().position(end);
    }
}
