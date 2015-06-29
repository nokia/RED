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
 * Matcher responsible for handling {@code SPACE} and {@code TABULATOR}
 * including also {@code DOUBLE_SPACES} case.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotSingleCharTokenType#SINGLE_SPACE
 * @see RobotSingleCharTokenType#SINGLE_TABULATOR
 * @see RobotWordType#DOUBLE_SPACE
 */
public class WhitespaceMatcher implements ISingleCharTokenMatcher {

    @Override
    public boolean match(TokenOutput tokenOutput, CharBuffer tempBuffer,
            int charIndex) {
        boolean wasUsed = false;
        boolean shouldBeHandleAsSingleSpace = true;

        char c = tempBuffer.get(charIndex);
        RobotSingleCharTokenType type = RobotSingleCharTokenType.getToken(c);

        if (type == RobotSingleCharTokenType.SINGLE_SPACE) {
            List<RobotToken> tokens = tokenOutput.getTokens();
            if (!tokens.isEmpty()) {
                RobotToken lastRobotToken = tokens.get(tokens.size() - 1);
                if (lastRobotToken.getType() == RobotSingleCharTokenType.SINGLE_SPACE) {
                    IRobotTokenType doubleSpacesType = RobotWordType.getToken("  ");
                    if (doubleSpacesType != null) {
                        shouldBeHandleAsSingleSpace = false;
                        replaceLastSingleSpaceByDoubleSpace(tokenOutput, c,
                                tokens, lastRobotToken, doubleSpacesType);
                    }
                }
            }

            if (shouldBeHandleAsSingleSpace) {
                StringBuilder str = new StringBuilder(" ");
                RobotToken spaceToken = new RobotToken(
                        tokenOutput.getCurrentMarker(), str);
                spaceToken.setType(type);
                tokenOutput.setCurrentMarker(spaceToken.getEndPosition());
                tokenOutput.getTokensPosition().put(type, tokens.size());
                tokens.add(spaceToken);
            }

            wasUsed = true;
        } else if (type == RobotSingleCharTokenType.SINGLE_TABULATOR) {
            List<RobotToken> tokens = tokenOutput.getTokens();
            RobotToken tabulatorToken = new RobotToken(
                    tokenOutput.getCurrentMarker(),
                    new StringBuilder().append(type.getThisTokenChar()));
            tabulatorToken.setType(type);
            tokenOutput.setCurrentMarker(tabulatorToken.getEndPosition());
            tokenOutput.getTokensPosition().put(type, tokens.size());
            tokens.add(tabulatorToken);

            wasUsed = true;
        }

        return wasUsed;
    }


    private void replaceLastSingleSpaceByDoubleSpace(TokenOutput tokenOutput,
            char c, List<RobotToken> tokens, RobotToken lastRobotToken,
            IRobotTokenType doubleSpacesType) {
        RobotToken doubleSpaces = new RobotToken(
                lastRobotToken.getStartPosition(), lastRobotToken.getText()
                        .append(c));
        doubleSpaces.setType(doubleSpacesType);
        tokenOutput.setCurrentMarker(doubleSpaces.getEndPosition());
        LinkedListMultimap<IRobotTokenType, Integer> tokensPosition = tokenOutput
                .getTokensPosition();
        List<Integer> listSingleSpace = tokensPosition
                .get(RobotSingleCharTokenType.SINGLE_SPACE);
        listSingleSpace.remove(listSingleSpace.size() - 1);
        tokensPosition.put(RobotWordType.DOUBLE_SPACE, tokens.size() - 1);
        tokens.set(tokens.size() - 1, doubleSpaces);
    }

}
