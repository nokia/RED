package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedList;
import java.util.List;


/**
 * Designed for context, which took place only in multiple lines i.e. settings
 * table.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see OneLineRobotContext
 * @see ContextBuilder
 */
public class ManyLinesRobotContext implements IContextElement {

    private List<IContextElement> childContexts = new LinkedList<>();
    private IContextElementType type = ComplexRobotContextType.UNDECLARED_COMMENT;
    private IContextElement parentContext = null;


    public void addNextLineContext(IContextElement newElement) {
        this.childContexts.add(newElement);
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
}
