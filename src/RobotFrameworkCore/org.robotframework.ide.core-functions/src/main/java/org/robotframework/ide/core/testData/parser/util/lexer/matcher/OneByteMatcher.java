package org.robotframework.ide.core.testData.parser.util.lexer.matcher;

/**
 * Matcher, which check if single one byte is as expected.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 */
public class OneByteMatcher extends AbstractSimpleByteMatcher {

    public OneByteMatcher(final byte expectedByte) {
        super(expectedByte);
    }


    @Override
    public boolean areBytesMatch(byte expected, byte got) {
        return expected == got;
    }


    @Override
    public String toString() {
        return "OneByteMatcher [expectedByte=" + (char) this.getExpectedByte()
                + "]";
    }
}
