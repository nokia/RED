package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.RobotToken;


/**
 * Designed for context, which took place only in one line i.e. comments, it is
 * also i.e. single element like it could be many variables in one line and it
 * will represent only single one.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see AggregatedOneLineRobotContexts
 * @see ContextBuilder
 */
public class OneLineSingleRobotContextPart implements IContextElement {

    private List<RobotToken> contextTokens = new LinkedList<>();
    private IContextElementType type = SimpleRobotContextType.UNDECLARED_COMMENT;
    private AggregatedOneLineRobotContexts parentContext = null;
    private final int lineNumber;


    public OneLineSingleRobotContextPart(final int lineNumber) {
        this.lineNumber = lineNumber;
    }


    public void addNextToken(RobotToken newToken) {
        this.contextTokens.add(newToken);
    }


    public void removeAllContextTokens() {
        contextTokens.clear();
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


    /**
     * 
     * @param context
     *            the parent context for this class is
     *            {@link AggregatedOneLineRobotContexts}
     */
    public void setParent(AggregatedOneLineRobotContexts context) {
        this.parentContext = context;
    }


    @Override
    public IContextElement getParent() {
        return parentContext;
    }


    @Override
    public void setParent(IContextElement context) {
        if (context == null
                || context instanceof AggregatedOneLineRobotContexts) {
            setParent((AggregatedOneLineRobotContexts) context);
        } else {
            throw new IllegalArgumentException("Context should be instance of "
                    + AggregatedOneLineRobotContexts.class + ", but was "
                    + context.getClass());
        }
    }
}
