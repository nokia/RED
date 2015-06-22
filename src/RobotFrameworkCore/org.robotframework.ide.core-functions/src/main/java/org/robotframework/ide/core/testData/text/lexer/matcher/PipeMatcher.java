package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * Matcher responsible for handling {@code PIPE}.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotTokenType#SINGLE_PIPE
 */
public class PipeMatcher implements ISingleCharTokenMatcher {

    @Override
    public boolean match(TokenOutput tokenOutput, CharBuffer tempBuffer,
            int charIndex) {
        boolean wasUsed = false;

        char c = tempBuffer.get(charIndex);
        RobotType type = RobotTokenType.getToken(c);

        if (type == RobotTokenType.SINGLE_PIPE) {
            StringBuilder str = new StringBuilder().append(c);
            RobotToken pipeToken = new RobotToken(
                    tokenOutput.getCurrentMarker(), str);
            pipeToken.setType(type);
            tokenOutput.setCurrentMarker(pipeToken.getEndPosition());
            List<RobotToken> tokens = tokenOutput.getTokens();
            tokenOutput.getTokensPosition().put(type, tokens.size());
            tokens.add(pipeToken);

            wasUsed = true;
        }

        return wasUsed;
    }

}
