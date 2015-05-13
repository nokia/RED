package org.robotframework.ide.core.testData.text.matchers;

import org.robotframework.ide.core.testData.text.IMatcher;
import org.robotframework.ide.core.testData.text.LinearFilePosition;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenType;

public class AsteriskMatcher implements IMatcher {

    @Override
    public RobotToken match(int currentChar, LinearFilePosition positionInLine) {
        RobotToken resultToken = null;

        if (currentChar == '*') {
            resultToken = new RobotToken(RobotTokenType.ASTERISK,
                    positionInLine);
            resultToken.setEndPos(positionInLine);
        }

        return resultToken;
    }
}
