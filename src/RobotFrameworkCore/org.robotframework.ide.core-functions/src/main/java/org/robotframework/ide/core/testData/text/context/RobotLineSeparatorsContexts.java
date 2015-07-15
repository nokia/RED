package org.robotframework.ide.core.testData.text.context;

import java.util.List;

import com.google.common.collect.LinkedListMultimap;


/**
 * Collector for all possible line separators.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see AggregatedOneLineRobotContexts
 * @see ContextBuilder
 */
public class RobotLineSeparatorsContexts implements IContextElement {

    public static final IContextElementType PIPE_SEPARATOR_TYPE = SimpleRobotContextType.PIPE_SEPARATED;
    public static final IContextElementType WHITESPACE_SEPARATOR_TYPE = SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED;
    public static final IContextElementType PRETTY_ALIGNMENT_TYPE = SimpleRobotContextType.PRETTY_ALIGN;

    private LinkedListMultimap<IContextElementType, IContextElement> handledElements = LinkedListMultimap
            .create();
    private IContextElementType type = ComplexRobotContextType.SEPARATORS;
    private IContextElement parentContext = null;
    private final int lineNumber;


    public RobotLineSeparatorsContexts(final int lineNumber) {
        this.lineNumber = lineNumber;
    }


    public int getLineNumber() {
        return lineNumber;
    }


    public void addNextSeparators(final List<IContextElement> separators) {
        if (separators != null && !separators.isEmpty()) {
            IContextElementType type = separators.get(0).getType();
            if (type == PIPE_SEPARATOR_TYPE
                    || type == WHITESPACE_SEPARATOR_TYPE) {
                handledElements.putAll(type, separators);
            }
        }
    }


    public LinkedListMultimap<IContextElementType, IContextElement> getFoundSeperatorsExcludeType() {
        return handledElements;
    }


    public List<IContextElement> getPipeSeparators() {
        return handledElements.get(PIPE_SEPARATOR_TYPE);
    }


    public List<IContextElement> getWhitespaceSeparators() {
        return handledElements.get(WHITESPACE_SEPARATOR_TYPE);
    }


    @Override
    public IContextElementType getType() {
        return type;
    }


    @Override
    public void setParent(IContextElement context) {
        this.parentContext = context;
    }


    @Override
    public IContextElement getParent() {
        return this.parentContext;
    }


    @Override
    public String toString() {
        return String
                .format("RobotLineSeparatorsContexts [handledElements=%s, type=%s, parentContext=%s]",
                        handledElements, type, parentContext);
    }

}
