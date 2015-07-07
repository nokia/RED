package org.robotframework.ide.core.testData.text.context.recognizer;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;


/**
 * Check if current line contains literal list with optional position marker
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
 * @see RobotSingleCharTokenType#SINGLE_LIST_BEGIN_AT
 * @see SimpleRobotContextType#LIST_VARIABLE
 */
public class ListVariableRecognizer extends AVariableRecognizer {

    public ListVariableRecognizer() {
        super(SimpleRobotContextType.LIST_VARIABLE,
                RobotSingleCharTokenType.SINGLE_LIST_BEGIN_AT);
    }
}
