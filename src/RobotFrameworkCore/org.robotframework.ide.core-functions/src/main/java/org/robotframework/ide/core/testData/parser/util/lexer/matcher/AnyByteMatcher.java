package org.robotframework.ide.core.testData.parser.util.lexer.matcher;

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
