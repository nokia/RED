package org.robotframework.ide.core.testData.text.contexts;

import org.robotframework.ide.core.testData.text.AParserContext;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenType;
import org.robotframework.ide.core.testData.text.Separator;


public class SeparatorContext extends AParserContext {

    private boolean isClosed = false;
    private boolean isValid = false;
    private int numberOfPipes = 0;
    private int numberOfTabs = 0;
    private int numberOfSpaces = 0;


    @Override
    public boolean offerToken(RobotToken token, Separator sep) {
        boolean wasAdded = false;

        if (!isClosed) {
            RobotTokenType type = token.getType();
            if (type == RobotTokenType.PIPE) {

            } else if (type == RobotTokenType.SPACE) {

            } else if (type == RobotTokenType.TAB) {

            } else {

            }
        }

        return wasAdded;
    }


    @Override
    public boolean isClosed() {
        return isClosed;
    }


    @Override
    public boolean shouldBeTakenInAccount() {
        return isValid;
    }
}
