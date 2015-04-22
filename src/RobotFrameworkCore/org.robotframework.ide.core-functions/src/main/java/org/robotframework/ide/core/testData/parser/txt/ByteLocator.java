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


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + begin;
        result = prime * result
                + ((byteStream == null) ? 0 : byteStream.hashCode());
        result = prime * result + end;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ByteLocator other = (ByteLocator) obj;
        if (begin != other.begin)
            return false;
        if (byteStream == null) {
            if (other.byteStream != null)
                return false;
        } else if (!byteStream.equals(other.byteStream))
            return false;
        if (end != other.end)
            return false;
        return true;
    }
}
