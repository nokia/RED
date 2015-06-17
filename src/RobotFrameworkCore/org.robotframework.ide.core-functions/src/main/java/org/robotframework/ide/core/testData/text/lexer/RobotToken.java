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

    private RobotType type = RobotTokenType.UNKNOWN;
    private final LinearPositionMarker startPosition;
    private final StringBuilder text;
    private final LinearPositionMarker endPosition;


    /**
     * Constructor, which set end position marker base on text length in
     * parameter {@code text}. Note: the begin type of token is set to
     * {@link RobotTokenType#UNKNOWN}
     * 
     * @param start
     *            where token was found
     * @param text
     *            token text
     */
    public RobotToken(final LinearPositionMarker start, final StringBuilder text) {
        this.startPosition = start;
        this.text = text;
        this.endPosition = new LinearPositionMarker(start.getLine(),
                start.getColumn() + text.length());
    }


    /**
     * Note: the begin type of token is set to {@link RobotTokenType#UNKNOWN}
     * 
     * @param start
     *            where token was found
     * @param text
     *            token text
     * @param end
     *            where token end ups
     */
    public RobotToken(final LinearPositionMarker start,
            final StringBuilder text, final LinearPositionMarker end) {
        this.startPosition = start;
        this.text = text;
        this.endPosition = end;
    }


    public RobotType getType() {
        return type;
    }


    public void setType(RobotType type) {
        this.type = type;
    }


    public LinearPositionMarker getStartPosition() {
        return startPosition;
    }


    public StringBuilder getText() {
        return text;
    }


    public LinearPositionMarker getEndPosition() {
        return endPosition;
    }


    @Override
    public String toString() {
        return String
                .format("RobotToken [type=%s, startPosition=%s, text=%s, endPosition=%s]",
                        type, startPosition, text, endPosition);
    }
}
