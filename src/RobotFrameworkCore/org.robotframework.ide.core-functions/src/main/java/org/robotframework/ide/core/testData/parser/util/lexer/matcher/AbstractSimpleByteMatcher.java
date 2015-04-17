package org.robotframework.ide.core.testData.parser.util.lexer.matcher;

import org.robotframework.ide.core.testData.parser.util.lexer.DataMarked;
import org.robotframework.ide.core.testData.parser.util.lexer.IMatcher;
import org.robotframework.ide.core.testData.parser.util.lexer.MatchResult;
import org.robotframework.ide.core.testData.parser.util.lexer.MatchResult.MatchStatus;
import org.robotframework.ide.core.testData.parser.util.lexer.Position;


/**
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 */
public abstract class AbstractSimpleByteMatcher implements IMatcher {

    private final byte expectedByte;


    public AbstractSimpleByteMatcher(final byte expectedByte) {
        this.expectedByte = expectedByte;
    }


    /**
     * @param expected
     * @param got
     * @return
     */
    public abstract boolean areBytesMatch(byte expected, byte got);


    public byte getExpectedByte() {
        return this.expectedByte;
    }


    @Override
    public MatchResult match(DataMarked dataWithPosition) {
        MatchResult result = new MatchResult(this, MatchStatus.NOT_FOUND);
        Position resultPosition = result.getPosition();

        byte[] data = dataWithPosition.getData();
        Position pos = dataWithPosition.getPosition();

        int byteIndex = pos.getStart();
        String validationMessage = checkLengthValid(dataWithPosition);
        if (validationMessage == null) {
            if (areBytesMatch(data[byteIndex], expectedByte)) {
                result.setStatus(MatchStatus.FOUND);
                resultPosition.setStart(byteIndex);
                resultPosition.setEnd(byteIndex);
            } else {
                result.setStatus(MatchStatus.NOT_FOUND);
                resultPosition.setStart(pos.getStart());
                resultPosition.setEnd(pos.getEnd());
            }
        } else {
            result.addMessage(validationMessage);
            resultPosition.setStart(pos.getStart());
            resultPosition.setEnd(pos.getEnd());
        }

        return result;
    }


    private String checkLengthValid(DataMarked dataWithPosition) {
        String errorMessage = null;
        byte[] data = dataWithPosition.getData();
        Position pos = dataWithPosition.getPosition();

        int start = pos.getStart();
        int end = pos.getEnd();

        if (data.length > 0) {
            if (start < 0) {
                errorMessage = "Start position " + start + " is below zero.";
            } else if (end < 0) {
                errorMessage = "End position " + end + " is below zero.";
            } else if (start > end) {
                errorMessage = "Start position " + start
                        + " is greater than end position " + end;
            } else if (end >= data.length) {
                errorMessage = "Do not have enough data to read, expected end position "
                        + end + " but we have " + data.length + " bytes.";
            } else if (start == end) {
                // ok
                errorMessage = null;
            }
        } else {
            errorMessage = "No data available for matching byte ["
                    + (char) expectedByte + "]";
        }
        return errorMessage;
    }
}
