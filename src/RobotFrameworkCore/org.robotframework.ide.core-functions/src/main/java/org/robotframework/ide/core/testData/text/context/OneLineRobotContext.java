package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.RobotToken;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public class OneLineRobotContext implements IContextElement {

    private List<RobotToken> contextTokens = new LinkedList<>();
    private IContextElementType type = SimpleRobotContextType.UNDECLARED_COMMENT;
    private ManyLinesRobotContext parentContext = null;
    private int lineNumber = -1;


    public OneLineRobotContext(final int lineNumber) {
        this.lineNumber = lineNumber;
    }


    public void addNextToken(RobotToken newToken) {
        this.contextTokens.add(newToken);
    }


    public List<RobotToken> getContextTokens() {
        return contextTokens;
    }


    @Override
    public IContextElementType getType() {
        return type;
    }


    public void setType(SimpleRobotContextType type) {
        this.type = type;
    }


    public int getLineNumber() {
        return lineNumber;
    }


    public ManyLinesRobotContext getParentContext() {
        return parentContext;
    }


    public void setParentContext(ManyLinesRobotContext parentContext) {
        this.parentContext = parentContext;
    }
}
