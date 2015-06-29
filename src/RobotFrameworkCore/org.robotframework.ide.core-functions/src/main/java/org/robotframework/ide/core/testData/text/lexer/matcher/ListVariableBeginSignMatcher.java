package org.robotframework.ide.core.testData.text.lexer.matcher;

import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;


/**
 * Matcher responsible for handling {@code AT_SIGN}.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotSingleCharTokenType#SINGLE_LIST_BEGIN_AT
 */
public class ListVariableBeginSignMatcher extends AOnlyMapCharToToken {

    public ListVariableBeginSignMatcher() {
        super(RobotSingleCharTokenType.SINGLE_LIST_BEGIN_AT);
    }
}
