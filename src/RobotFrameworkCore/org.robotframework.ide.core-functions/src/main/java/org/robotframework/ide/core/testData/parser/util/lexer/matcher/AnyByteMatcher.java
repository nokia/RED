package org.robotframework.ide.core.testData.parser.util.lexer.matcher;

/**
 * Represents any kind of data, but anything must exists inside provided data
 * chunk.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 */
public class AnyByteMatcher extends AbstractSimpleByteMatcher {

    public AnyByteMatcher() {
        this((byte) ' ');
    }


    public AnyByteMatcher(byte expectedByte) {
        super(expectedByte);
    }


    @Override
    public boolean areBytesMatch(byte expected, byte got) {
        return true;
    }
}
