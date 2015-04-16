package org.robotframework.ide.core.testData.parser.util.lexer.matcher;

import org.robotframework.ide.core.testData.parser.util.lexer.DataMarked;
import org.robotframework.ide.core.testData.parser.util.lexer.IMatcher;
import org.robotframework.ide.core.testData.parser.util.lexer.MatchResult;
import org.robotframework.ide.core.testData.parser.util.lexer.MatchResult.MatchStatus;
import org.robotframework.ide.core.testData.parser.util.lexer.Position;


public class OneByteMatcher implements IMatcher {

    private final byte expectedByte;


    public OneByteMatcher(final byte expectedByte) {
        this.expectedByte = expectedByte;
    }


    @Override
    public MatchResult match(DataMarked dataWithPosition) {
        MatchResult result = new MatchResult(this, MatchStatus.NOT_FOUND);

        byte[] data = dataWithPosition.getData();
        Position pos = dataWithPosition.getPosition();

        int start = pos.getStart();
        String validationMessage = checkLengthValid(dataWithPosition);
        if (validationMessage == null) {
            if (data[start] == expectedByte) {
                result.setStatus(MatchStatus.FOUND);
            } else {
                result.setStatus(MatchStatus.NOT_FOUND);
            }
        } else {
            result.addMessage(validationMessage);
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
            if (start > end) {
                errorMessage = "Start position " + start
                        + " is greater than end position " + end;
            } else if (start < 0) {
                errorMessage = "Start position " + start + " is below zero.";
            } else if (end < 0) {
                errorMessage = "End position " + end + " is below zero.";
            } else if (end >= data.length) {
                errorMessage = "Do not enough data to read, expected end position "
                        + end + " but we have " + data.length + " bytes.";
            } else if (start == end) {
                // ok
                errorMessage = null;
            }
        } else {
            errorMessage = "No data available for byte [" + expectedByte + "]";
        }
        return errorMessage;
    }


    public byte getExpectedByte() {
        return this.expectedByte;
    }
}
