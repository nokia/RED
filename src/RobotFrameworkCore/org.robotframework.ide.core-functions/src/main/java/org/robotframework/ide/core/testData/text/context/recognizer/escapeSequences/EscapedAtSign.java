package org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;


/**
 * Check if current line contains escaped 'at' sign {@code '\@'}
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotSingleCharTokenType#SINGLE_LIST_BEGIN_AT
 * 
 * @see SimpleRobotContextType#ESCAPED_AT_SIGN
 */
public class EscapedAtSign extends AEscapedSpecialSingleCharTokens {

    public EscapedAtSign() {
        super(SimpleRobotContextType.ESCAPED_AT_SIGN, '@', '@',
                RobotSingleCharTokenType.SINGLE_LIST_BEGIN_AT);
    }
}
