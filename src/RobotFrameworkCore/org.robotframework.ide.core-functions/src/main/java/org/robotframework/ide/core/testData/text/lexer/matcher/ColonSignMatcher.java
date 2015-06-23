package org.robotframework.ide.core.testData.text.lexer.matcher;

import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;


/**
 * Matcher responsible for handling {@code COLON [:]}.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotTokenType#SINGLE_COLON
 */
public class ColonSignMatcher extends AOnlyMapCharToToken {

    public ColonSignMatcher() {
        super(RobotTokenType.SINGLE_COLON);
    }
}
