package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * Extracted functionality related to mapping directly char to type, without any
 * extra logic related to duplication of the same char or coexistence with
 * others.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ColonSignMatcher
 * @see DictionaryVariableBeginSignMatcher
 * @see EnvironmentVariableBeginSignMatcher
 * @see EqualSignMatcher
 * @see IndexBeginSquareSignMatcher
 * @see IndexEndSquareSignMatcher
 * @see ListVariableBeginSignMatcher
 * @see PipeMatcher
 * @see QuoteMarkSignMatcher
 * @see ScalarVariableBeginSignMatcher
 * @see VariableBeginCurlySignMatcher
 * @see VariableEndCurlySignMatcher
 */
public abstract class AOnlyMapCharToToken implements ISingleCharTokenMatcher {

    private final IRobotTokenType acceptedType;


    /**
     * 
     * @param acceptedType
     *            type which should be mapped to token directly
     */
    protected AOnlyMapCharToToken(final IRobotTokenType acceptedType) {
        this.acceptedType = acceptedType;
    }


    @Override
    public boolean match(TokenOutput tokenOutput, CharBuffer tempBuffer,
            int charIndex) {
        boolean wasUsed = false;

        char c = tempBuffer.get(charIndex);
        IRobotTokenType type = RobotSingleCharTokenType.getToken(c);

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
