package org.robotframework.ide.core.testData.text.write;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.RobotLine;


public class TestCaseDumper implements IElementDumper {

    @Override
    public boolean canDump(IRobotLineElement elem) {
        return false;
    }


    @Override
    public String dump(RobotLine line, int elementIndex) {
        // TODO Auto-generated method stub
        return null;
    }

}
