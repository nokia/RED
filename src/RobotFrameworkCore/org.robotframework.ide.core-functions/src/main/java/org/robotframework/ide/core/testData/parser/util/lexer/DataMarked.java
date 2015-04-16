package org.robotframework.ide.core.testData.parser.util.lexer;

/**
 * Mapping between data and position [start; end] in them.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class DataMarked {

    private final byte[] data;
    private final Position position;


    public DataMarked(final byte[] data, final Position position) {
        this.data = data;
        this.position = position;
    }


    public byte[] getData() {
        return data;
    }


    public Position getPosition() {
        return position;
    }
}
