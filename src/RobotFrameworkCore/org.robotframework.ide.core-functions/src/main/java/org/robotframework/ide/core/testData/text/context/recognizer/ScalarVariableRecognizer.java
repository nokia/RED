package org.robotframework.ide.core.testData.text.context.recognizer;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;


/**
 * Check if current line contains literal scalar with optional position marker
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotSingleCharTokenType#SINGLE_VARIABLE_BEGIN_CURLY_BRACKET
 * @see RobotSingleCharTokenType#SINGLE_VARIABLE_END_CURLY_BRACKET
 * @see RobotSingleCharTokenType#SINGLE_POSITION_INDEX_BEGIN_SQUARE_BRACKET
 * @see RobotSingleCharTokenType#SINGLE_POSITION_INDEX_END_SQUARE_BRACKET
 * 
 * @see RobotSingleCharTokenType#SINGLE_SCALAR_BEGIN_DOLLAR
 * @see SimpleRobotContextType#SCALAR_VARIABLE
 */
public class ScalarVariableRecognizer extends AVariableRecognizer {

    public ScalarVariableRecognizer() {
        super(SimpleRobotContextType.SCALAR_VARIABLE,
                RobotSingleCharTokenType.SINGLE_SCALAR_BEGIN_DOLLAR);
    }
}
