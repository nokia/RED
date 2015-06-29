package org.robotframework.ide.core.testData.text.lexer.matcher;

import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;


/**
 * Matcher responsible for handling {@code DOLAR_SIGN}.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotSingleCharTokenType#SINGLE_SCALAR_BEGIN_DOLLAR
 */
public class ScalarVariableBeginSignMatcher extends AOnlyMapCharToToken {

    public ScalarVariableBeginSignMatcher() {
        super(RobotSingleCharTokenType.SINGLE_SCALAR_BEGIN_DOLLAR);
    }
}
