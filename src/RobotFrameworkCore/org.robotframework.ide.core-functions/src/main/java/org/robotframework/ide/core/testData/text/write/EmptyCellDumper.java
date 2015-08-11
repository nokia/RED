package org.robotframework.ide.core.testData.text.write;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.RobotLine;


public class EmptyCellDumper implements IElementDumper {

    @Override
    public boolean canDump(IRobotLineElement elem) {
        String text = elem.getText().toString();
        return (text.equals(""));
    }


    @Override
    public String dump(RobotLine line, int elementIndex) {
        return "\\";
    }
}
