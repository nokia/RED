package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.RobotToken;


public class OneLineRobotContext {

    private List<RobotToken> contextTokens = new LinkedList<>();
    private SimpleRobotContextType type = SimpleRobotContextType.UNDECLARED_COMMENT;
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


    public SimpleRobotContextType getType() {
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
