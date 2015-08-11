package org.robotframework.ide.core.testData.text.write;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.RobotLine;


public interface IElementDumper {

    boolean canDump(final IRobotLineElement elem);


    String dump(final RobotLine line, int elementIndex);
}
