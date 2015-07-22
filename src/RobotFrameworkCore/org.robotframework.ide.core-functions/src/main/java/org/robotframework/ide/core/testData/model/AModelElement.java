package org.robotframework.ide.core.testData.model;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.RobotLine;


public abstract class AModelElement implements IOptional {

    private RobotLine declaringRobotLine;
    private List<IRobotLineElement> includingElements = new LinkedList<>();


    public RobotLine getDeclaringRobotLine() {
        return declaringRobotLine;
    }


    public void setDeclaringRobotLine(RobotLine declaringRobotLine) {
        this.declaringRobotLine = declaringRobotLine;
    }


    public List<IRobotLineElement> getIncludingElements() {
        return includingElements;
    }


    public void setIncludingElements(List<IRobotLineElement> includingElements) {
        this.includingElements = includingElements;
    }
}
