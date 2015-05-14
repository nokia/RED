package org.robotframework.ide.core.testData.text.contexts;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.AParserContext;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenType;
import org.robotframework.ide.core.testData.text.Separator;


public class ParserContextHandler {

    private final List<AParserContext> openContexts = new LinkedList<>();
    private final List<AParserContext> closeContexts = new LinkedList<>();

    private final List<RobotToken> tempTokenStore = new LinkedList<>();


    public void giveToken(RobotToken token, Separator sep) {
        RobotTokenType tokenType = token.getType();
    }
}
