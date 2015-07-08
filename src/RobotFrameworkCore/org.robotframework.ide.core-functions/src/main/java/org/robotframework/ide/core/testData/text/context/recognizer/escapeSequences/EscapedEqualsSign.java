package org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;


/**
 * Check if current line contains escaped dollar sign {@code '\='}
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotSingleCharTokenType#SINGLE_EQUAL
 * 
 * @see SimpleRobotContextType#ESCAPED_EQUALS_SIGN
 */
public class EscapedEqualsSign extends AEscapedSpecialSingleCharTokens {

    public EscapedEqualsSign() {
        super(SimpleRobotContextType.ESCAPED_EQUALS_SIGN, '=', '=',
                RobotSingleCharTokenType.SINGLE_EQUAL);
    }
}
