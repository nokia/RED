package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;

import com.google.common.collect.LinkedListMultimap;


/**
 * Trying to match <CR>+<LF>, <CR>, <LF>, <LF>+<CR> chars as line separators
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class EndOfLineMatcher implements ISingleCharTokenMatcher {

    @Override
    public boolean match(TokenOutput tokenOutput, CharBuffer tempBuffer,
            int charIndex) {
        boolean wasUsed = false;

        char c = tempBuffer.get(charIndex);
        RobotTokenType type = RobotTokenType.getToken(c);

        if (type == RobotTokenType.CARRIAGE_RETURN) {
            RobotType oppositeType = RobotTokenType.LINE_FEED;
            wasUsed = handleLineSeparator(tokenOutput, type, oppositeType);
        } else if (type == RobotTokenType.LINE_FEED) {
            RobotType oppositeType = RobotTokenType.CARRIAGE_RETURN;
            wasUsed = handleLineSeparator(tokenOutput, type, oppositeType);
        }

        return wasUsed;
    }


    private boolean handleLineSeparator(TokenOutput tokenOutput,
            RobotTokenType type, RobotType oppositeType) {
        boolean wasUsed = false;

        LinkedListMultimap<RobotType, Integer> tokensPosition = tokenOutput
                .getTokensPosition();
        List<RobotToken> tokens = tokenOutput.getTokens();
        if (!tokens.isEmpty()) {
            int nrOfTokens = tokens.size();
            if (tokens.get(nrOfTokens - 1).getType() == RobotTokenType.END_OF_LINE) {
                List<Integer> currentTypeIndexes = tokensPosition.get(type);
                int numberOfCurrentTypeTokens = currentTypeIndexes.size();
                List<Integer> oppositeTypeIndexes = tokensPosition
                        .get(oppositeType);
                int numberOfOpTypeTokens = oppositeTypeIndexes.size();

                if (numberOfOpTypeTokens > numberOfCurrentTypeTokens) {
                    // LF+CR or CR+LF case
                    if (tokens.get(nrOfTokens - 2).getType() == oppositeType) {
                        tokens.remove(nrOfTokens - 1);
                        List<Integer> eolIndexes = tokensPosition
                                .get(RobotTokenType.END_OF_LINE);
                        eolIndexes.remove(eolIndexes.size() - 1);
                        RobotToken oppositeTypeTokenLast = tokens
                                .get(nrOfTokens - 2);

                        tokenOutput.setCurrentMarker(oppositeTypeTokenLast
                                .getEndPosition());
                        addLineEnd(tokenOutput, type);
                        wasUsed = true;
                    }
                } else {
                    // normal case: CR, LF or CR+LF, LF+CR separator
                    addLineEnd(tokenOutput, type);
                    wasUsed = true;
                }
            } else {
                addLineEnd(tokenOutput, type);
                wasUsed = true;
            }
        } else {
            addLineEnd(tokenOutput, type);
            wasUsed = true;
        }
        return wasUsed;
    }


    private void addLineEnd(TokenOutput tokenOutput, RobotTokenType type) {
        LinearPositionMarker pos = tokenOutput.getCurrentMarker();
        List<RobotToken> tokens = tokenOutput.getTokens();
        LinkedListMultimap<RobotType, Integer> tokensPosition = tokenOutput
                .getTokensPosition();
        int column = pos.getColumn();
        if (column > LinearPositionMarker.THE_FIRST_COLUMN) {
            column += 1;
        }

        LinearPositionMarker newMarker = new LinearPositionMarker(
                pos.getLine(), column);

        RobotToken endToken = new RobotToken(newMarker, new StringBuilder(
                type.toWrite()));
        endToken.setType(type);

        tokens.add(endToken);
        tokensPosition.put(type, tokens.size() - 1);

        RobotToken eolToken = new RobotToken(endToken.getEndPosition(), null,
                endToken.getEndPosition());
        eolToken.setType(RobotTokenType.END_OF_LINE);

        tokens.add(eolToken);
        tokensPosition.put(RobotTokenType.END_OF_LINE, tokens.size() - 1);

        tokenOutput.setCurrentMarker(LinearPositionMarker
                .createMarkerForFirstColumn(pos.getLine() + 1));
    }
}
