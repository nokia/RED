package org.robotframework.ide.core.testData.text.lexer;

/**
 * Memory holder of single part of file in example Robot Framework table name -
 * Settings, which could be transfer to Robot Framework file model.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class RobotToken {

    private IRobotTokenType type = RobotSingleCharTokenType.UNKNOWN;
    private final FilePosition startPosition;
    private final StringBuilder text;
    private final FilePosition endPosition;


    /**
     * Constructor, which set end position marker base on text length in
     * parameter {@code text}. Note: the begin type of token is set to
     * {@link RobotSingleCharTokenType#UNKNOWN}
     * 
     * @param start
     *            where token was found
     * @param text
     *            token text
     */
    public RobotToken(final FilePosition start, final StringBuilder text) {
        this.startPosition = start;
        this.text = text;
        this.endPosition = new FilePosition(start.getLine(),
                start.getColumn() + text.length());
    }


    /**
     * Note: the begin type of token is set to
     * {@link RobotSingleCharTokenType#UNKNOWN}
     * 
     * @param start
     *            where token was found
     * @param text
     *            token text
     * @param end
     *            where token end ups
     */
    public RobotToken(final FilePosition start,
            final StringBuilder text, final FilePosition end) {
        this.startPosition = start;
        this.text = text;
        this.endPosition = end;
    }


    public IRobotTokenType getType() {
        return type;
    }


    public void setType(IRobotTokenType type) {
        this.type = type;
    }


    public FilePosition getStartPosition() {
        return startPosition;
    }


    public StringBuilder getText() {
        return text;
    }


    public FilePosition getEndPosition() {
        return endPosition;
    }


    @Override
    public String toString() {
        return String
                .format("RobotToken [type=%s, startPosition=%s, text=%s, endPosition=%s]",
                        type, startPosition, text, endPosition);
    }
}
