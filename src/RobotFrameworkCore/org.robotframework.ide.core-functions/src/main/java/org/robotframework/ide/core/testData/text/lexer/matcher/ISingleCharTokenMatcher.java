package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;

import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * Include functionality need for matching single char as token.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 */
public interface ISingleCharTokenMatcher {

    /**
     * 
     * @param tokenOutput
     * @param tempBuffer
     *            where read chars are stored
     * @param charIndex
     *            current index of char in buffer
     * @return an information if token was processed
     */
    boolean match(final TokenOutput tokenOutput, final CharBuffer tempBuffer,
            final int charIndex);
}
