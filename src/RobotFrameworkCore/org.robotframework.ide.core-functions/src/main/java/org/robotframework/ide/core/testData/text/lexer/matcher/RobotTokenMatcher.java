package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;

import com.google.common.collect.LinkedListMultimap;


public class RobotTokenMatcher {

    private RobotTokenType previousFoundType = RobotTokenType.UNKNOWN;

    public volatile static Map<String, RobotTokenType> charToRobotTokenType;
    static {
        charToRobotTokenType = new HashMap<>();
        charToRobotTokenType.put("\r", RobotTokenType.CARRITAGE_RETURN);
        charToRobotTokenType.put("\n", RobotTokenType.LINE_FEED);
        charToRobotTokenType.put("\t", RobotTokenType.SINGLE_TABULATOR);
        charToRobotTokenType.put(" ", RobotTokenType.SINGLE_SPACE);
        charToRobotTokenType = Collections
                .unmodifiableMap(charToRobotTokenType);
    }


    public void offerChar(
            final LinkedListMultimap<RobotTokenType, RobotToken> tokens,
            final CharBuffer tempBuffer, final int charIndex) {
        char c = tempBuffer.get(charIndex);

    }
}
