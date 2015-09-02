package org.robotframework.ide.core.testData.text.read;

import java.util.LinkedList;
import java.util.List;


public class RobotLine {

    private int lineNumber = -1;
    private List<IRobotLineElement> lineElements = new LinkedList<>();
    private String uuid;


    public RobotLine(int lineNumber) {
        this.lineNumber = lineNumber;
    }


    public void setFileUUID(String uuid) {
        this.uuid = uuid;
    }


    public String getFileUUID() {
        return uuid;
    }


    public List<IRobotLineElement> getLineElements() {
        return lineElements;
    }


    public void setLineElements(List<IRobotLineElement> lineElements) {
        this.lineElements = lineElements;
    }


    public void addLineElement(IRobotLineElement lineElement) {
        this.lineElements.add(lineElement);
    }


    public int getLineNumber() {
        return lineNumber;
    }


    @Override
    public String toString() {
        return String.format("RobotLine [lineNumber=%s, lineElements=%s]",
                lineNumber, lineElements);
    }

}
