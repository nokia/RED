package org.robotframework.ide.core.testData.text.context;

/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public interface IContextElement {

    IContextElementType getType();


    void setParent(IContextElement context);


    IContextElement getParent();
}
