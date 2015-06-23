package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


public class AOnlyMapCharToToken implements ISingleCharTokenMatcher {

    private final RobotType acceptedType;


    public AOnlyMapCharToToken(final RobotType acceptedType) {
        this.acceptedType = acceptedType;
    }


    @Override
    public boolean match(TokenOutput tokenOutput, CharBuffer tempBuffer,
            int charIndex) {
        boolean wasUsed = false;

        char c = tempBuffer.get(charIndex);
        RobotType type = RobotTokenType.getToken(c);

        if (type == this.acceptedType) {
            StringBuilder str = new StringBuilder().append(c);
            RobotToken token = new RobotToken(tokenOutput.getCurrentMarker(),
                    str);
            token.setType(type);
            tokenOutput.setCurrentMarker(token.getEndPosition());
            List<RobotToken> tokens = tokenOutput.getTokens();
            tokenOutput.getTokensPosition().put(type, tokens.size());
            tokens.add(token);

            wasUsed = true;
        }

        return wasUsed;
    }
}
