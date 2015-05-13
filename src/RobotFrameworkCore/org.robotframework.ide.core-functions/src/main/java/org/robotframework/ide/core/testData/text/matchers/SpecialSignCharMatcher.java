package org.robotframework.ide.core.testData.text.matchers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.text.IMatcher;
import org.robotframework.ide.core.testData.text.LinearFilePosition;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenType;


public class SpecialSignCharMatcher implements IMatcher {

    private final static Map<Character, RobotTokenType> tokens = new LinkedHashMap<>();
    static {
        tokens.put('|', RobotTokenType.PIPE);
        tokens.put('@', RobotTokenType.AT);
        tokens.put('#', RobotTokenType.HASH);
        tokens.put('$', RobotTokenType.DOLAR_SIGN);
        tokens.put('%', RobotTokenType.PERCENT);
        tokens.put('^', RobotTokenType.CARET);
        tokens.put('&', RobotTokenType.AMPERSAND);
        tokens.put('-', RobotTokenType.MINUS);
        tokens.put('_', RobotTokenType.UNDERSCORE);
        tokens.put('+', RobotTokenType.PLUS);
        tokens.put('=', RobotTokenType.EQUALS);
        tokens.put('\\', RobotTokenType.BACKSLASH);
        tokens.put('/', RobotTokenType.FORWARD_SLASH);
        tokens.put(':', RobotTokenType.COLON);
        tokens.put(';', RobotTokenType.SEMICOLON);
        tokens.put('\"', RobotTokenType.QUOTATION_MARK);
        tokens.put('\'', RobotTokenType.APOSTROPHE);
        tokens.put('<', RobotTokenType.LESS_THAN);
        tokens.put('>', RobotTokenType.GREATER_THAN);
        tokens.put(',', RobotTokenType.COMMA);
        tokens.put('.', RobotTokenType.DOT);
        tokens.put('?', RobotTokenType.QUESTION_MARK);
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
