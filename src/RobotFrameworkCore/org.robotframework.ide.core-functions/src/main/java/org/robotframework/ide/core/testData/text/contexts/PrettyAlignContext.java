package org.robotframework.ide.core.testData.text.contexts;

import org.robotframework.ide.core.testData.text.AParserContext;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.Separator;


public class PrettyAlignContext extends AParserContext {

    @Override
    public boolean offerToken(RobotToken token, Separator sep) {
        return false;
    }


    @Override
    public boolean isClosed() {
        return false;
    }


    @Override
    public boolean shouldBeTakenInAccount() {
        // TODO Auto-generated method stub
        return false;
    }
}
