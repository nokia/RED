package org.robotframework.ide.core.testData.text.context.recognizer;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * Check if current line contains literal character with hex value hh - null
 * byte: \x00, ä: \xE4
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotWordType
 * 
 * @see SimpleRobotContextType#CHAR_WITH_BYTE_HEX_VALUE
 */
public class CharacterWithByteHexValue extends ACharacterAsHexValue {

    public CharacterWithByteHexValue() {
        super(SimpleRobotContextType.CHAR_WITH_BYTE_HEX_VALUE, 'x', 2);
    }
}
