package org.robotframework.ide.core.testData;

import java.io.File;

import org.robotframework.ide.core.testData.model.IRobotFile;


public interface IRobotFileDumper {

    boolean canDumpFile(final File file);


    void dump(final File robotFile, final IRobotFile model) throws Exception;
}
