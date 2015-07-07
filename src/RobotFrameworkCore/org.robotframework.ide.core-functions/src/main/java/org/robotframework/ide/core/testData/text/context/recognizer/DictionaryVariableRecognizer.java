package org.robotframework.ide.core.testData.text.context.recognizer;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;


/**
 * Check if current line contains literal dictionary (map) with optional
 * position marker
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
 * @see RobotSingleCharTokenType#SINGLE_DICTIONARY_BEGIN_AMPERSAND
 * @see SimpleRobotContextType#DICTIONARY_VARIABLE
 */
public class DictionaryVariableRecognizer extends AVariableRecognizer {

    public DictionaryVariableRecognizer() {
        super(SimpleRobotContextType.DICTIONARY_VARIABLE,
                RobotSingleCharTokenType.SINGLE_DICTIONARY_BEGIN_AMPERSAND);
    }
}
