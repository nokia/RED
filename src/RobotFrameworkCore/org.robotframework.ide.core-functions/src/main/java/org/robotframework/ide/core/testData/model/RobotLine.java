package org.robotframework.ide.core.testData.model;

import java.util.LinkedList;
import java.util.List;


public class RobotLine {

    private List<LineElement> elements = new LinkedList<>();


    public List<LineElement> getElements() {
        return elements;
    }


    public void setElements(List<LineElement> elements) {
        this.elements = elements;
    }


    @Override
    public String toString() {
        return String.format("RobotLine [elements=%s]", elements);
    }

}
