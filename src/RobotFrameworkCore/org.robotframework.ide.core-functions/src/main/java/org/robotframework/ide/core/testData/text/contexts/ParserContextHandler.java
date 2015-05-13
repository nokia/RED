package org.robotframework.ide.core.testData.text.contexts;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.AParserContext;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.Separator;


public class ParserContextHandler {

    private final List<AParserContext> openContexts = new LinkedList<>();
    private final List<AParserContext> closeContexts = new LinkedList<>();


    public ParserContextHandler() {
        // jezeli bedzie spacja a nie ma separatora kontekstu do go dodaj jesli
        // jest to nic nie rob
    }


    public void giveToken(RobotToken token, Separator sep) {

    }
}
