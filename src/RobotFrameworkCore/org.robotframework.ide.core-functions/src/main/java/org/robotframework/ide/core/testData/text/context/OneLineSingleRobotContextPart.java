package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.recognizer.keywordTable.KeywordTableRobotContextType;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.SettingTableRobotContextType;
import org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable.TestCaseTableRobotContextType;
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


    /**
     * 
     * @param type
     *            acceptable parameters are type from {@code @see}, in case
     *            parameter is null default
     *            {@link SimpleRobotContextType#UNDECLARED_COMMENT} will be set
     * @throws UnsupportedOperationException
     *             in case {@code type} is not supported
     * 
     * @see SimpleRobotContextType
     * @see KeywordTableRobotContextType
     * @see SettingTableRobotContextType
     * @see TestCaseTableRobotContextType
     */
    public void setType(IContextElementType type) {
        if (type != null) {
            if (type instanceof SimpleRobotContextType) {
                setType((SimpleRobotContextType) type);
            } else if (type instanceof KeywordTableRobotContextType) {
                setType((KeywordTableRobotContextType) type);
            } else if (type instanceof SettingTableRobotContextType) {
                setType((SettingTableRobotContextType) type);
            } else if (type instanceof TestCaseTableRobotContextType) {
                setType((TestCaseTableRobotContextType) type);
            } else {
                throw new UnsupportedOperationException("Type "
                        + type.getClass() + " is not supported by this class.");
            }
        } else {
            this.type = SimpleRobotContextType.UNDECLARED_COMMENT;
        }
    }


    public void setType(SimpleRobotContextType type) {
        this.type = type;
    }


    public void setType(KeywordTableRobotContextType type) {
        this.type = type;
    }


    public void setType(SettingTableRobotContextType type) {
        this.type = type;
    }


    public void setType(TestCaseTableRobotContextType type) {
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


    @Override
    public String toString() {
        return String
                .format("OneLineSingleRobotContextPart [contextTokens=%s, type=%s, parentContext=%s, lineNumber=%s]",
                        contextTokens, type, parentContext, lineNumber);
    }

}
