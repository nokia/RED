package org.robotframework.ide.core.testData.text.lexer.matcher;

import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;


/**
 * Matcher responsible for handling {@code SQUARE_BRACKET_OPEN}.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotTokenType#SINGLE_POSSITION_INDEX_BEGIN_SQUARE_BRACKET
 */
public class IndexBeginSquareSignMatcher extends AOnlyMapCharToToken {

    public IndexBeginSquareSignMatcher() {
        super(RobotTokenType.SINGLE_POSSITION_INDEX_BEGIN_SQUARE_BRACKET);
    }
}
