package org.robotframework.ide.core.testData.text.lexer.matcher;

import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;


/**
 * Matcher responsible for handling {@code CURLY_BRACKET_CLOSE}.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotSingleCharTokenType#SINGLE_VARIABLE_END_CURLY_BRACKET
 */
public class VariableEndCurlySignMatcher extends AOnlyMapCharToToken {

    public VariableEndCurlySignMatcher() {
        super(RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET);
    }
}
