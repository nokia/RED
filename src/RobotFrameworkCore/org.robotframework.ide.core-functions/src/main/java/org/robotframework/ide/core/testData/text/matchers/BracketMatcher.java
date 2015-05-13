package org.robotframework.ide.core.testData.text.matchers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.text.IMatcher;
import org.robotframework.ide.core.testData.text.LinearFilePosition;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenType;


public class BracketMatcher implements IMatcher {

    private final static Map<Character, RobotTokenType> tokens = new LinkedHashMap<>();
    static {
        tokens.put('(', RobotTokenType.OPEN_CIRCLE_BRACKET);
        tokens.put(')', RobotTokenType.CLOSE_CIRCLE_BRACKET);
        tokens.put('{', RobotTokenType.OPEN_CURLY_BRACKET);
        tokens.put('}', RobotTokenType.CLOSE_CURLY_BRACKET);
        tokens.put('[', RobotTokenType.OPEN_SQUARE_BRACKET);
        tokens.put(']', RobotTokenType.CLOSE_SQUARE_BRACKET);
    }


    @Override
    public RobotToken match(int currentChar, LinearFilePosition positionInLine) {
        RobotToken token = null;

        char current = (char) currentChar;
        if (tokens.containsKey(current)) {
            token = new RobotToken(tokens.get(current), positionInLine);
            token.setEndPos(positionInLine);
        }

        return token;
    }
}
