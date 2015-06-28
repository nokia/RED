package org.robotframework.ide.core.testData.text.context;

import java.util.Iterator;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public class TokensLineIterator implements Iterator<LineTokenPosition> {

    private final TokenOutput tokenOutput;
    private LineTokenPosition currentPosition = new LineTokenPosition(-1, -1, 0);
    private final List<Integer> listOfLineEnds;
    private final int numberOfLineEnds;


    public TokensLineIterator(final TokenOutput tokenOutput) {
        this.tokenOutput = tokenOutput;
        listOfLineEnds = tokenOutput.getTokensPosition().get(
                RobotTokenType.END_OF_LINE);
        numberOfLineEnds = listOfLineEnds.size();
        checkNextLineIndex();
    }


    @Override
    public boolean hasNext() {
        return (currentPosition != null);
    }


    @Override
    public LineTokenPosition next() {
        LineTokenPosition nextElement = currentPosition;
        checkNextLineIndex();

        return nextElement;
    }


    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }


    private void checkNextLineIndex() {
        if (currentPosition != null) {
            if (currentPosition.getLineNumber() == 0) {
                // check if has many lines
                if (numberOfLineEnds > 0) {
                    currentPosition = new LineTokenPosition(0,
                            listOfLineEnds.get(0),
                            LinearPositionMarker.THE_FIRST_LINE);
                } else {
                    // case when only one line exits in file, but the first we
                    // ensure that file is not empty
                    int numberOfTokens = tokenOutput.getTokens().size();
                    if (numberOfTokens > 0) {
                        currentPosition = new LineTokenPosition(0,
                                numberOfTokens,
                                LinearPositionMarker.THE_FIRST_LINE);
                    } else {
                        currentPosition = null;
                    }
                }
            } else {
                int currentLine = currentPosition.getLineNumber();
                int nextLineNumber = currentLine + 1;
                // check if we have more lines still
                if (currentLine < numberOfLineEnds) {
                    currentPosition = new LineTokenPosition(
                            currentPosition.getEnd() + 1,
                            listOfLineEnds.get(currentLine), nextLineNumber);
                } else if (currentLine == numberOfLineEnds) {
                    int numberOfTokens = tokenOutput.getTokens().size();
                    if (currentPosition.getEnd() + 1 < numberOfTokens) {
                        currentPosition = new LineTokenPosition(
                                currentPosition.getEnd() + 1, numberOfTokens,
                                nextLineNumber);
                    } else {
                        currentPosition = null;
                    }
                } else {
                    currentPosition = null;
                }
            }
        }
    }

    public static class LineTokenPosition {

        private final int start;
        private final int end;
        private final int lineNumber;


        public LineTokenPosition(final int start, final int end,
                final int lineNumber) {
            this.start = start;
            this.end = end;
            this.lineNumber = lineNumber;
        }


        public int getStart() {
            return start;
        }


        public int getEnd() {
            return end;
        }


        public int getLineNumber() {
            return lineNumber;
        }
    }
}
