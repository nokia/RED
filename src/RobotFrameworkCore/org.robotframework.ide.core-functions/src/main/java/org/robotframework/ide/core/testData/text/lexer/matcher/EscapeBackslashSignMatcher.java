package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;

import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * Matcher responsible for handling {@code BACKSLASH} and including also
 * {@code DOUBLE_ESCAPE_BACKSLASH} case, where escape is used as escape for
 * following by them escape.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotWordType#DOUBLE_ESCAPE_BACKSLASH
 */
public class EscapeBackslashSignMatcher implements ISingleCharTokenMatcher {

    @Override
    public boolean match(TokenOutput tokenOutput, CharBuffer tempBuffer,
            int charIndex) {
        boolean wasUsed = false;
        boolean shouldBeHandleAsSingleBackslash = true;

        return wasUsed;
    }
}
