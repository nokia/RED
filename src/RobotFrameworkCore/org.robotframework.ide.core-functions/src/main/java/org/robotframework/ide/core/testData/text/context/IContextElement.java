package org.robotframework.ide.core.testData.text.context;

/**
 * Define interface, for context elements.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public interface IContextElement {

    IContextElementType getType();


    /**
     * Child shouldn't define, who is their parent. This responsibility is for
     * parent or context builder.
     * 
     * @param context
     *            parent for this context
     */
    void setParent(IContextElement context);


    IContextElement getParent();
}
