package org.robotframework.ide.core.testData.text.matchers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.text.IMatcher;
import org.robotframework.ide.core.testData.text.LinearFilePosition;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenType;


public class LineEndMatcher implements IMatcher {

    private static final Map<Character, RobotTokenType> tokens = new LinkedHashMap<>();
    static {
        tokens.put('\r', RobotTokenType.CARRIAGE_RETURN);
        tokens.put('\n', RobotTokenType.LINE_FEED);
        tokens.put('\u000B', RobotTokenType.VERTICAL_TAB);
        tokens.put('\u000C', RobotTokenType.FORM_FEED);
        tokens.put('\u0085', RobotTokenType.NEXT_LINE);
        tokens.put('\u2028', RobotTokenType.LINE_SEPARATOR);
        tokens.put('\u2029', RobotTokenType.PARAGRAPH_SEPARATOR);
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
