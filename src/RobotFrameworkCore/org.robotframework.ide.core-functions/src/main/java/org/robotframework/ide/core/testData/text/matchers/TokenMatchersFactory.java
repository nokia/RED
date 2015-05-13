package org.robotframework.ide.core.testData.text.matchers;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.IMatcher;
import org.robotframework.ide.core.testData.text.LinearFilePosition;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenType;


public class TokenMatchersFactory {

    private final List<IMatcher> matchers = new LinkedList<>();


    public TokenMatchersFactory() {
        matchers.add(new CharacterMatcher());
        matchers.add(new WhitespaceMatcher());
        matchers.add(new SpecialSignCharMatcher());
        matchers.add(new BracketMatcher());
        matchers.add(new LineEndMatcher());
        matchers.add(new DigitMatcher());
        matchers.add(new AsteriskMatcher());
    }


    public RobotToken match(int currentChar, LinearFilePosition position) {
        RobotToken token = null;

        for (IMatcher matcher : matchers) {
            token = matcher.match(currentChar, position);
            if (token != null) {
                break;
            }
        }

        if (token == null) {
            token = new RobotToken(RobotTokenType.UNKNOWN_CHARACTER, position);
            token.setText("" + (char) currentChar);
            token.setEndPos(position);
        }

        return token;
    }
}
