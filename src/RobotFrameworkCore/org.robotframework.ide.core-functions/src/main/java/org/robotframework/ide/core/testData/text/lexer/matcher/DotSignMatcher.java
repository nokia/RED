package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;

import com.google.common.collect.LinkedListMultimap;


/**
 * Matcher responsible for handling {@code DOT} including also
 * {@code EMPTY_CELL_DOTS} and {@code CONTINOUE_PREVIOUS_LINE_DOTS} cases. In
 * case it will be more dots close to each other than it is declared in
 * {@code CONTINOUE_PREVIOUS_LINE_DOTS} then token will become
 * {@code MORE_THAN_THREE_DOTS} token.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotSingleCharTokenType#SINGLE_DOT
 * @see RobotWordType#EMPTY_CELL_DOTS
 * @see RobotWordType#CONTINOUE_PREVIOUS_LINE_DOTS
 * @see MultipleCharTokenType#MORE_THAN_THREE_DOTS
 */
public class DotSignMatcher implements ISingleCharTokenMatcher {

    @Override
    public boolean match(TokenOutput tokenOutput, CharBuffer tempBuffer,
            int charIndex) {
        boolean wasUsed = false;
        boolean shouldBeHandleAsSingleDot = true;

        char c = tempBuffer.get(charIndex);
        IRobotTokenType type = RobotSingleCharTokenType.getToken(c);

        if (type == RobotSingleCharTokenType.SINGLE_DOT) {
            List<RobotToken> tokens = tokenOutput.getTokens();
            if (!tokens.isEmpty()) {
                RobotToken lastRobotToken = tokens.get(tokens.size() - 1);
                IRobotTokenType lastRobotTokenType = lastRobotToken.getType();

                if (lastRobotTokenType == RobotSingleCharTokenType.SINGLE_DOT) {
                    mergeLastDotTokenTypeToNewType(tokenOutput, c,
                            lastRobotToken, RobotWordType.EMPTY_CELL_DOTS);
                    shouldBeHandleAsSingleDot = false;
                } else if (lastRobotTokenType == RobotWordType.EMPTY_CELL_DOTS) {
                    mergeLastDotTokenTypeToNewType(tokenOutput, c,
                            lastRobotToken,
                            RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS);
                    shouldBeHandleAsSingleDot = false;
                } else if (lastRobotTokenType == RobotWordType.CONTINOUE_PREVIOUS_LINE_DOTS) {
                    mergeLastDotTokenTypeToNewType(tokenOutput, c,
                            lastRobotToken,
                            MultipleCharTokenType.MORE_THAN_THREE_DOTS);
                    shouldBeHandleAsSingleDot = false;
                } else if (lastRobotTokenType == MultipleCharTokenType.MORE_THAN_THREE_DOTS) {
                    addNewDotToGroupedSameTokenType(tokenOutput, c);
                    shouldBeHandleAsSingleDot = false;
                }
            }

            if (shouldBeHandleAsSingleDot) {
                StringBuilder text = new StringBuilder().append(c);
                RobotToken dotToken = new RobotToken(
                        tokenOutput.getCurrentMarker(), text);
                dotToken.setType(type);
                tokenOutput.setCurrentMarker(dotToken.getEndPosition());
                tokenOutput.getTokensPosition().put(type, tokens.size());
                tokens.add(dotToken);
            }

            wasUsed = true;
        }

        return wasUsed;
    }


    private void mergeLastDotTokenTypeToNewType(TokenOutput tokenOutput,
            char c, RobotToken lastRobotToken, IRobotTokenType newType) {
        List<RobotToken> tokens = tokenOutput.getTokens();
        RobotToken manyDots = new RobotToken(lastRobotToken.getStartPosition(),
                lastRobotToken.getText().append(c));
        manyDots.setType(newType);
        tokenOutput.setCurrentMarker(manyDots.getEndPosition());
        LinkedListMultimap<IRobotTokenType, Integer> tokensPosition = tokenOutput
                .getTokensPosition();
        List<Integer> previousTokenTypeList = tokensPosition.get(lastRobotToken
                .getType());
        previousTokenTypeList.remove(previousTokenTypeList.size() - 1);
        tokensPosition.put(newType, tokens.size() - 1);
        tokens.set(tokens.size() - 1, manyDots);
    }


    private void addNewDotToGroupedSameTokenType(TokenOutput tokenOutput, char c) {
        List<RobotToken> tokens = tokenOutput.getTokens();
        RobotToken lastRobotToken = tokens.get(tokens.size() - 1);
        RobotToken groupedDots = new RobotToken(
                lastRobotToken.getStartPosition(), lastRobotToken.getText()
                        .append(c));
        groupedDots.setType(MultipleCharTokenType.MORE_THAN_THREE_DOTS);
        tokenOutput.setCurrentMarker(groupedDots.getEndPosition());
        tokens.set(tokens.size() - 1, groupedDots);
    }
}
