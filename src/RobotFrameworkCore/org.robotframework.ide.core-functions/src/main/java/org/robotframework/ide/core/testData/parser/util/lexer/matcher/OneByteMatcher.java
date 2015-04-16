package org.robotframework.ide.core.testData.parser.util.lexer.matcher;

/**
 * 
 * @author wypych
 * 
 */
public class OneByteMatcher extends AbstractSimpleByteMatcher {

    /**
     * @param expectedByte
     */
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
