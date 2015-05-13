package org.robotframework.ide.core.testData.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.matchers.TokenMatchersFactory;


public class TxtRobotFileLexer {

    private final TokenMatchersFactory matcher = new TokenMatchersFactory();


    public List<RobotToken> recognizeTokens(InputStreamReader charStream)
            throws IOException {
        List<RobotToken> tokens = new LinkedList<>();
        BufferedReader lineReader = new BufferedReader(charStream);

        int currentChar = -1;
        int currentLine = 0;
        int currentColumn = 0;

        RobotToken previousTokenNotStart = null;
        while((currentChar = lineReader.read()) != -1) {
            RobotToken matchedToken = matcher.match(currentChar,
                    new LinearFilePosition(currentLine, currentColumn));
            tokens.add(matchedToken);

            if (isEndOfLine(matchedToken)) {
                if (isCrLf(matchedToken, previousTokenNotStart)) {
                    fixCrLfCase(tokens, previousTokenNotStart, matchedToken);

                    currentColumn = -1;
                } else {
                    currentLine++;
                    currentColumn = -1;
                    RobotToken startLine = new RobotToken(
                            RobotTokenType.START_LINE, new LinearFilePosition(
                                    currentLine, 0));
                    tokens.add(startLine);
                }
            }

            previousTokenNotStart = matchedToken;
            currentColumn++;
        }

        LinearFilePosition currentLinearPos = new LinearFilePosition(
                currentLine, currentColumn);
        addTheFirstStartTokenIfWasAnyData(tokens, currentLinearPos);
        addEndOfFile(tokens, currentChar, currentLinearPos);

        return tokens;
    }


    private void fixCrLfCase(List<RobotToken> tokens,
            RobotToken previousTokenNotStart, RobotToken matchedToken) {
        // remove last start token information
        tokens.remove(tokens.size() - 1);
        LinearFilePosition lastNotStartPos = previousTokenNotStart
                .getStartPos();
        LinearFilePosition newPosition = new LinearFilePosition(
                lastNotStartPos.getLine(), lastNotStartPos.getColumn() + 1);
        RobotToken fixed = new RobotToken(matchedToken.getType(), newPosition);
        fixed.setEndPos(newPosition);
        tokens.add(tokens.size() - 1, fixed);
    }


    private boolean isCrLf(RobotToken current, RobotToken previous) {
        boolean isCrLfCase = false;

        if (current != null && previous != null) {
            RobotTokenType previousType = previous.getType();
            RobotTokenType currentType = current.getType();

            isCrLfCase = (previousType == RobotTokenType.CARRIAGE_RETURN)
                    && (currentType == RobotTokenType.LINE_FEED);
        }

        return isCrLfCase;
    }


    private boolean isEndOfLine(RobotToken token) {
        RobotTokenType type = token.getType();
        return type == RobotTokenType.CARRIAGE_RETURN
                || type == RobotTokenType.LINE_FEED
                || type == RobotTokenType.VERTICAL_TAB
                || type == RobotTokenType.FORM_FEED
                || type == RobotTokenType.NEXT_LINE
                || type == RobotTokenType.LINE_SEPARATOR
                || type == RobotTokenType.PARAGRAPH_SEPARATOR;
    }


    private void addEndOfFile(List<RobotToken> tokens, int currentChar,
            LinearFilePosition linearPos) {
        if (currentChar == -1) {
            RobotToken eofToken = new RobotToken(RobotTokenType.END_OF_FILE,
                    linearPos);
            tokens.add(eofToken);
        }
    }


    private void addTheFirstStartTokenIfWasAnyData(List<RobotToken> tokens,
            LinearFilePosition linearPos) {
        if (linearPos.getLine() > 0 || linearPos.getColumn() > 0) {
            RobotToken firstStartLine = new RobotToken(
                    RobotTokenType.START_LINE, new LinearFilePosition(0, 0));
            tokens.add(0, firstStartLine);
        }
    }
}
