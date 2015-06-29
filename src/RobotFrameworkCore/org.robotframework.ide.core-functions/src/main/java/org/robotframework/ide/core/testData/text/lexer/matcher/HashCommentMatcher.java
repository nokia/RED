package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;

import com.google.common.collect.LinkedListMultimap;


/**
 * Handler for hash ('#') comment cases - it join asterisks in case few of them
 * appears close to each other.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotSingleCharTokenType#SINGLE_COMMENT_HASH
 * @see MultipleCharTokenType#MANY_COMMENT_HASHS
 */
public class HashCommentMatcher implements ISingleCharTokenMatcher {

    @Override
    public boolean match(TokenOutput tokenOutput, CharBuffer tempBuffer,
            int charIndex) {
        boolean wasUsed = false;
        boolean shouldBeHandleAsSingleHash = true;

        char c = tempBuffer.get(charIndex);
        IRobotTokenType type = RobotSingleCharTokenType.getToken(c);
        if (type == RobotSingleCharTokenType.SINGLE_COMMENT_HASH) {
            List<RobotToken> tokens = tokenOutput.getTokens();

            if (!tokens.isEmpty()) {
                RobotToken lastRobotToken = tokens.get(tokens.size() - 1);
                if (MultipleCharTokenType.MANY_COMMENT_HASHS
                        .isFromThisGroup(lastRobotToken)) {
                    mergeLastHashsTokenWithCurrent(tokenOutput, c,
                            lastRobotToken);
                    shouldBeHandleAsSingleHash = false;
                }
            }

            if (shouldBeHandleAsSingleHash) {
                StringBuilder text = new StringBuilder().append(c);
                RobotToken hashCommentToken = new RobotToken(
                        tokenOutput.getCurrentMarker(), text);
                hashCommentToken.setType(type);
                tokenOutput.setCurrentMarker(hashCommentToken.getEndPosition());
                tokenOutput.getTokensPosition().put(type, tokens.size());
                tokens.add(hashCommentToken);
            }

            wasUsed = true;
        }

        return wasUsed;
    }


    private void mergeLastHashsTokenWithCurrent(TokenOutput tokenOutput,
            char c, RobotToken lastRobotToken) {
        IRobotTokenType lastRobotTokenType = lastRobotToken.getType();
        if (lastRobotTokenType == MultipleCharTokenType.MANY_COMMENT_HASHS) {
            mergeLastHashsTokenWithCurrent(tokenOutput, c, lastRobotToken,
                    MultipleCharTokenType.MANY_COMMENT_HASHS);
        } else if (lastRobotTokenType == RobotSingleCharTokenType.SINGLE_COMMENT_HASH) {
            mergeLastHashsTokenWithCurrent(tokenOutput, c, lastRobotToken,
                    RobotSingleCharTokenType.SINGLE_COMMENT_HASH);
        }
    }


    private void mergeLastHashsTokenWithCurrent(TokenOutput tokenOutput,
            char c, RobotToken lastRobotToken, MultipleCharTokenType type) {
        List<RobotToken> tokens = tokenOutput.getTokens();
        RobotToken groupedHash = new RobotToken(
                lastRobotToken.getStartPosition(), lastRobotToken.getText()
                        .append(c));
        groupedHash.setType(MultipleCharTokenType.MANY_COMMENT_HASHS);
        tokenOutput.setCurrentMarker(groupedHash.getEndPosition());
        tokens.set(tokens.size() - 1, groupedHash);
    }


    private void mergeLastHashsTokenWithCurrent(TokenOutput tokenOutput,
            char c, RobotToken lastRobotToken, RobotSingleCharTokenType type) {
        List<RobotToken> tokens = tokenOutput.getTokens();
        RobotToken groupedHash = new RobotToken(
                lastRobotToken.getStartPosition(), lastRobotToken.getText()
                        .append(c));
        groupedHash.setType(MultipleCharTokenType.MANY_COMMENT_HASHS);
        tokenOutput.setCurrentMarker(groupedHash.getEndPosition());
        LinkedListMultimap<IRobotTokenType, Integer> tokensPosition = tokenOutput
                .getTokensPosition();
        List<Integer> listSingleHashs = tokensPosition.get(type);
        listSingleHashs.remove(listSingleHashs.size() - 1);
        tokensPosition.put(MultipleCharTokenType.MANY_COMMENT_HASHS,
                tokens.size() - 1);
        tokens.set(tokens.size() - 1, groupedHash);
    }
}
