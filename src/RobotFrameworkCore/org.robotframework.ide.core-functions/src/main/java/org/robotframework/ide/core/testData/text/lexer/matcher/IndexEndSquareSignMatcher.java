package org.robotframework.ide.core.testData.text.lexer.matcher;

import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;


/**
 * Matcher responsible for handling {@code SQUARE_BRACKET_CLOSE}.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotTokenType#SINGLE_POSSITION_INDEX_END_SQUARE_BRACKET
 */
public class IndexEndSquareSignMatcher extends AOnlyMapCharToToken {

    public IndexEndSquareSignMatcher() {
        super(RobotTokenType.SINGLE_POSSITION_INDEX_END_SQUARE_BRACKET);
    }
}
