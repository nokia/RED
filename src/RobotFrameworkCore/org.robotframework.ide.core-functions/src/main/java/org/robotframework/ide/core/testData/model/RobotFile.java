package org.robotframework.ide.core.testData.model;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.RobotLine;


public class RobotFile {

    private final List<RobotLine> fileContent = new LinkedList<>();


    public List<RobotLine> getFileContent() {
        return fileContent;
    }


    public void addNewLine(final RobotLine line) {
        this.fileContent.add(line);
    }
}
