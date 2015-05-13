package org.robotframework.ide.core.testData.text.matchers;

import org.robotframework.ide.core.testData.text.IMatcher;
import org.robotframework.ide.core.testData.text.LinearFilePosition;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenType;

public class DigitMatcher implements IMatcher {

    @Override
    public RobotToken match(int currentChar, LinearFilePosition positionInLine) {
        RobotToken token = null;

        if (Character.isDigit(currentChar)) {
            token = new RobotToken(RobotTokenType.DIGIT, positionInLine);
            token.setEndPos(positionInLine);
        }

        return token;
    }
}
