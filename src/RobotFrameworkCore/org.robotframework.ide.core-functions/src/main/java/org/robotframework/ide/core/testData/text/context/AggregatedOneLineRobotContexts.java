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

    private List<IContextElement> separators = new LinkedList<>();
    private List<IContextElement> childContexts = new LinkedList<>();
    private LinkedListMultimap<IContextElementType, IContextElement> handledElements = LinkedListMultimap
            .create();
    private IContextElementType type = ComplexRobotContextType.UNDECLARED_COMMENT;
    private IContextElement parentContext = null;


    public void addNextLineContext(IContextElement newElement) {
        this.childContexts.add(newElement);
        this.handledElements.put(newElement.getType(), newElement);
    }


    public void addNextTypeOfSeparators(final IContextElement separator) {
        this.separators.add(separator);
        this.handledElements.put(separator.getType(), separator);
    }


    public List<IContextElement> getChildContexts() {
        return childContexts;
    }


    public List<IContextElement> getSeparatorsForLine() {
        return separators;
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
}
