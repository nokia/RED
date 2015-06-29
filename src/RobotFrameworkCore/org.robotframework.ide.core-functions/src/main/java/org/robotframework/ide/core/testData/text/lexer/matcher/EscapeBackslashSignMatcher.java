package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;

import com.google.common.collect.LinkedListMultimap;


/**
 * Matcher responsible for handling {@code BACKSLASH} and including also
 * {@code DOUBLE_ESCAPE_BACKSLASH} case, where escape is used as escape for
 * following by them escape.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotWordType#DOUBLE_ESCAPE_BACKSLASH
 */
public class EscapeBackslashSignMatcher implements ISingleCharTokenMatcher {

    @Override
    public boolean match(TokenOutput tokenOutput, CharBuffer tempBuffer,
            int charIndex) {
        boolean wasUsed = false;
        boolean shouldBeHandleAsSingleBackslash = true;

        char c = tempBuffer.get(charIndex);
        IRobotTokenType type = RobotSingleCharTokenType.getToken(c);
        if (type == RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH) {
            List<RobotToken> tokens = tokenOutput.getTokens();
            if (!tokens.isEmpty()) {
                RobotToken lastRobotToken = tokens.get(tokens.size() - 1);
                if (lastRobotToken.getType() == RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH) {
                    IRobotTokenType doubleBackslashTokenType = RobotWordType
                            .getToken("" + c + c);
                    if (doubleBackslashTokenType != null) {
                        replaceLastSingleBackslashByEscapedBackslash(
                                tokenOutput, c, doubleBackslashTokenType);
                        shouldBeHandleAsSingleBackslash = false;
                    }
                }
            }

            if (shouldBeHandleAsSingleBackslash) {
                StringBuilder text = new StringBuilder().append(c);
                RobotToken backslashToken = new RobotToken(
                        tokenOutput.getCurrentMarker(), text);
                backslashToken.setType(type);
                tokenOutput.setCurrentMarker(backslashToken.getEndPosition());
                tokenOutput.getTokensPosition().put(type, tokens.size());
                tokens.add(backslashToken);
            }

            wasUsed = true;
        }

        return wasUsed;
    }


    private void replaceLastSingleBackslashByEscapedBackslash(
            TokenOutput tokenOutput, char c, IRobotTokenType doubleBackslashTokenType) {
        List<RobotToken> tokens = tokenOutput.getTokens();
        int lastTokenIndex = tokens.size() - 1;
        RobotToken lastRobotToken = tokens.get(lastTokenIndex);
        RobotToken doubleBackslash = new RobotToken(
                lastRobotToken.getStartPosition(), lastRobotToken.getText()
                        .append(c));
        doubleBackslash.setType(doubleBackslashTokenType);
        tokenOutput.setCurrentMarker(doubleBackslash.getEndPosition());
        LinkedListMultimap<IRobotTokenType, Integer> tokensPosition = tokenOutput
                .getTokensPosition();
        List<Integer> listSingleBackslashes = tokensPosition.get(lastRobotToken
                .getType());
        listSingleBackslashes.remove(listSingleBackslashes.size() - 1);
        tokensPosition.put(doubleBackslashTokenType, lastTokenIndex);
        tokens.set(lastTokenIndex, doubleBackslash);
    }
}
