package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;

import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


public interface ITokenMatcher {

    boolean match(final TokenOutput tokenOutput, final CharBuffer tempBuffer,
            final int charIndex);
}
