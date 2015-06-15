package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;

import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;

import com.google.common.collect.LinkedListMultimap;


public class RobotTokenMatcher {

    private RobotType previousFoundType = RobotTokenType.UNKNOWN;


    public void offerChar(
            final LinkedListMultimap<RobotType, RobotToken> tokens,
            final CharBuffer tempBuffer, final int charIndex) {
        char c = tempBuffer.get(charIndex);

    }
}
