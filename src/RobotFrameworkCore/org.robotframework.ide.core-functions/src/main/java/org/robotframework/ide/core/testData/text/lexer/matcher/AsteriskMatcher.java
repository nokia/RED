package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.GroupedSameTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;

import com.google.common.collect.LinkedListMultimap;


/**
 * Handler for asterisks cases - it join asterisks in case few of them appears
 * close to each other.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotTokenType#SINGLE_ASTERISK
 * @see GroupedSameTokenType#MANY_ASTERISKS
 */
public class AsteriskMatcher implements ISingleCharTokenMatcher {

    @Override
    public boolean match(TokenOutput tokenOutput, CharBuffer tempBuffer,
            int charIndex) {
        boolean wasUsed = false;
        boolean shouldBeHandleAsSingleAsterisk = true;

        char c = tempBuffer.get(charIndex);
        RobotType type = RobotTokenType.getToken(c);
        if (type == RobotTokenType.SINGLE_ASTERISK) {
            List<RobotToken> tokens = tokenOutput.getTokens();

            if (!tokens.isEmpty()) {
                RobotToken lastRobotToken = tokens.get(tokens.size() - 1);
                if (GroupedSameTokenType.MANY_ASTERISKS
                        .isFromThisGroup(lastRobotToken)) {
                    shouldBeHandleAsSingleAsterisk = false;
                    mergeLastAsterisksTokenWithCurrent(tokenOutput, c,
                            lastRobotToken);
                }
            }

            if (shouldBeHandleAsSingleAsterisk) {
                StringBuilder str = new StringBuilder().append(c);
                RobotToken asteriskToken = new RobotToken(
                        tokenOutput.getCurrentMarker(), str);
                asteriskToken.setType(type);
                tokenOutput.setCurrentMarker(asteriskToken.getEndPosition());
                tokenOutput.getTokensPosition().put(type, tokens.size());
                tokens.add(asteriskToken);
            }

            wasUsed = true;
        }

        return wasUsed;
    }


    private void mergeLastAsterisksTokenWithCurrent(TokenOutput tokenOutput,
            char c, RobotToken lastRobotToken) {
        RobotType lastRobotTokenType = lastRobotToken.getType();
        if (lastRobotTokenType == GroupedSameTokenType.MANY_ASTERISKS) {
            mergeLastAsterisksTokenWithCurrent(tokenOutput, c, lastRobotToken,
                    GroupedSameTokenType.MANY_ASTERISKS);
        } else if (lastRobotTokenType == RobotTokenType.SINGLE_ASTERISK) {
            mergeLastAsterisksTokenWithCurrent(tokenOutput, c, lastRobotToken,
                    RobotTokenType.SINGLE_ASTERISK);
        }
    }


    private void mergeLastAsterisksTokenWithCurrent(TokenOutput tokenOutput,
            char c, RobotToken lastRobotToken, GroupedSameTokenType type) {
        List<RobotToken> tokens = tokenOutput.getTokens();
        RobotToken groupedAsterisks = new RobotToken(
                lastRobotToken.getStartPosition(), lastRobotToken.getText()
                        .append(c));
        groupedAsterisks.setType(GroupedSameTokenType.MANY_ASTERISKS);
        tokenOutput.setCurrentMarker(groupedAsterisks.getEndPosition());
        tokens.set(tokens.size() - 1, groupedAsterisks);
    }


    private void mergeLastAsterisksTokenWithCurrent(TokenOutput tokenOutput,
            char c, RobotToken lastRobotToken, RobotTokenType type) {
        List<RobotToken> tokens = tokenOutput.getTokens();
        RobotToken groupedAsterisks = new RobotToken(
                lastRobotToken.getStartPosition(), lastRobotToken.getText()
                        .append(c));
        groupedAsterisks.setType(GroupedSameTokenType.MANY_ASTERISKS);
        tokenOutput.setCurrentMarker(groupedAsterisks.getEndPosition());
        LinkedListMultimap<RobotType, Integer> tokensPosition = tokenOutput
                .getTokensPosition();
        List<Integer> listSingleAsterisks = tokensPosition.get(type);
        listSingleAsterisks.remove(listSingleAsterisks.size() - 1);
        tokensPosition.put(GroupedSameTokenType.MANY_ASTERISKS,
                tokens.size() - 1);
        tokens.set(tokens.size() - 1, groupedAsterisks);
    }
}
