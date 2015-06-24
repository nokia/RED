package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedList;
import java.util.List;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class ManyLinesRobotContext {

    private List<OneLineRobotContext> childContexts = new LinkedList<>();
    private ComplexRobotContextType type = ComplexRobotContextType.UNDECLARED_COMMENT;


    public void addNextLineContext(OneLineRobotContext newLine) {
        this.childContexts.add(newLine);
    }


    public List<OneLineRobotContext> getChildContexts() {
        return childContexts;
    }


    public ComplexRobotContextType getType() {
        return type;
    }


    public void setType(ComplexRobotContextType type) {
        this.type = type;
    }
}
