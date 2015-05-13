package org.robotframework.ide.core.testData.text.matchers;

import org.robotframework.ide.core.testData.text.IMatcher;
import org.robotframework.ide.core.testData.text.LinearFilePosition;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenType;

public class CharacterMatcher implements IMatcher {

    @Override
    public RobotToken match(int currentChar, LinearFilePosition positionInLine) {
        RobotToken token = null;

        if (Character.isLetter(currentChar)) {
            token = new RobotToken(RobotTokenType.UNICODE_LETTER,
                    positionInLine);
            token.setEndPos(positionInLine);
            token.setText("" + (char) currentChar);
        }

        return token;
    }
}
