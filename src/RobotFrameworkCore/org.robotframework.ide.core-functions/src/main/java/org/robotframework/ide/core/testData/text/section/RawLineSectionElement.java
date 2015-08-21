package org.robotframework.ide.core.testData.text.section;

import org.robotframework.ide.core.testData.text.read.RobotLine;


public class RawLineSectionElement extends ASectionElement {

    private RobotLine rawLine;


    public RobotLine getRawLine() {
        return rawLine;
    }


    public void setRawLine(RobotLine rawLine) {
        this.rawLine = rawLine;
    }
}
