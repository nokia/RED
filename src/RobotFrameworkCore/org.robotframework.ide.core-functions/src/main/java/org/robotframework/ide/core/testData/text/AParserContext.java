package org.robotframework.ide.core.testData.text;

import java.util.LinkedList;
import java.util.List;


public abstract class AParserContext {

    protected final List<RobotToken> tokens = new LinkedList<>();


    public abstract boolean offerToken(RobotToken token, Separator sep);


    public abstract boolean isClosed();


    public abstract boolean shouldBeTakenInAccount();
}
