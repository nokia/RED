package org.robotframework.ide.core.testData.model.listener;

import java.util.List;

import org.robotframework.ide.core.testData.text.read.RobotLine;


public interface IRobotFile extends ITablesExclusion, ITablesInclusion,
        ITablesGetter {

    List<RobotLine> getFileContent();


    void addNewLine(RobotLine line);

}
