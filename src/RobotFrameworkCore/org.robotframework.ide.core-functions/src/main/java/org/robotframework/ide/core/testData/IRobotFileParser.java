package org.robotframework.ide.core.testData;

import java.io.File;

import org.robotframework.ide.core.testData.model.RobotFileOutput;


public interface IRobotFileParser {

    boolean canParseFile(final File file);


    RobotFileOutput parse(final File robotFile);
}
