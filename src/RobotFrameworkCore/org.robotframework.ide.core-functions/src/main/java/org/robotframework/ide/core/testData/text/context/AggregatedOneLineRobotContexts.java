package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.LinkedListMultimap;


/**
 * Designed for aggregation all possible contexts, for one line.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see OneLineSingleRobotContextPart
 * @see ContextBuilder
 */
public class AggregatedOneLineRobotContexts implements IContextElement {

    private RobotLineSeparatorsContexts separators = new RobotLineSeparatorsContexts();
    private List<IContextElement> childContexts = new LinkedList<>();
    private LinkedListMultimap<IContextElementType, IContextElement> handledElements = LinkedListMultimap
            .create();
    private IContextElementType type = ComplexRobotContextType.UNDECLARED_COMMENT;
    private IContextElement parentContext = null;


    public void addNextLineContext(IContextElement newElement) {
        this.childContexts.add(newElement);
        this.handledElements.put(newElement.getType(), newElement);
    }


    public LinkedListMultimap<IContextElementType, IContextElement> getChildContextTypes() {
        return handledElements;
    }


    public List<IContextElement> getChildContexts() {
        return childContexts;
    }


    @Override
    public IContextElementType getType() {
        return type;
    }


    public void setType(ComplexRobotContextType type) {
        this.type = type;
    }


    @Override
    public void setParent(IContextElement context) {
        this.parentContext = context;
    }


    @Override
    public IContextElement getParent() {
        return this.parentContext;
    }


    public RobotLineSeparatorsContexts getSeparators() {
        return separators;
    }


    public void setSeparators(RobotLineSeparatorsContexts separators) {
        this.separators = separators;
    }
}
