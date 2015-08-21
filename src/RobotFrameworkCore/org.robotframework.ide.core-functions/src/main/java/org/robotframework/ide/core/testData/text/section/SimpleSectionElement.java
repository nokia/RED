package org.robotframework.ide.core.testData.text.section;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.text.read.RobotLine;


public class SimpleSectionElement extends ASectionElement {

    private AModelElement modelElement;
    private List<RobotLine> usedLines = new LinkedList<>();


    public AModelElement getModelElement() {
        return modelElement;
    }


    public void setModelElement(final AModelElement modelElement) {
        this.modelElement = modelElement;
    }


    public List<RobotLine> getDeclarationLines() {
        return usedLines;
    }


    public void addUsedLine(final RobotLine line) {
        usedLines.add(line);
    }
}
