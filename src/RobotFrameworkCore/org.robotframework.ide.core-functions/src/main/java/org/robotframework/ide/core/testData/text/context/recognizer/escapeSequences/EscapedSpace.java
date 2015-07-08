package org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.helpers.Collection;


/**
 * Check if current line contains escaped space sign {@code '\ '}
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotSingleCharTokenType#SINGLE_SPACE
 * @see RobotWordType#DOUBLE_SPACE
 * 
 * @see SimpleRobotContextType#ESCAPED_SPACE
 */
public class EscapedSpace extends AEscapedSpecialSingleCharTokens {

    public EscapedSpace() {
        super(SimpleRobotContextType.ESCAPED_SPACE, ' ', ' ', Collection
                .createOfType(IRobotTokenType.class,
                        RobotSingleCharTokenType.SINGLE_SPACE,
                        RobotWordType.DOUBLE_SPACE));
    }
}
