package org.robotframework.ide.core.testData.model;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;


public abstract class AModelElement implements IOptional {

    private List<IRobotLineElement> includingElements = new LinkedList<>();


    public List<IRobotLineElement> getIncludingElements() {
        return includingElements;
    }


    public void setIncludingElements(List<IRobotLineElement> includingElements) {
        this.includingElements = includingElements;
    }
}
