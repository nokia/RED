package org.robotframework.ide.core.testData.text.matchers;

import org.robotframework.ide.core.testData.text.IMatcher;
import org.robotframework.ide.core.testData.text.LinearFilePosition;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenType;

public class WhitespaceMatcher implements IMatcher {

    @Override
    public RobotToken match(int currentChar, LinearFilePosition positionInLine) {
        RobotToken token = null;

        if (currentChar == ' ') {
            token = new RobotToken(RobotTokenType.SPACE, positionInLine);
        } else if (currentChar == '\t') {
            token = new RobotToken(RobotTokenType.TAB, positionInLine);
        }

        if (token != null) {
            token.setEndPos(positionInLine);
        }

        return token;
    }

}
