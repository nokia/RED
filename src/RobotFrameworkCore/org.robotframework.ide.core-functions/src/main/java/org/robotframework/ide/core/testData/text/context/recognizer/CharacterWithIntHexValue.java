package org.robotframework.ide.core.testData.text.context.recognizer;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * Check if current line contains literal character with hex value hhhhhhhh.
 * love hotel: \U0001f3e9
 * 
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotWordType
 * 
 * @see SimpleRobotContextType#CHAR_WITH_INT_HEX_VALUE
 */
public class CharacterWithIntHexValue extends ACharacterAsHexValue {

    public CharacterWithIntHexValue() {
        super(SimpleRobotContextType.CHAR_WITH_INT_HEX_VALUE, 'U', 8);
    }
}
