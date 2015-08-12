package org.robotframework.ide.core.testData;

import java.io.File;

import org.robotframework.ide.core.testData.model.RobotFile;


public interface IRobotFileDumper {

    boolean canDumpFile(final File file);


    void dump(final File robotFile, final RobotFile model) throws Exception;
}
