/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.IChildElement;
import org.robotframework.ide.core.testData.model.RobotFile;


public class RobotLine implements IChildElement<RobotFile> {

    private final RobotFile parent;
    private int lineNumber = -1;
    private List<IRobotLineElement> lineElements = new LinkedList<>();


    public RobotLine(int lineNumber, final RobotFile parent) {
        this.lineNumber = lineNumber;
        this.parent = parent;
    }


    public RobotFile getParent() {
        return parent;
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
