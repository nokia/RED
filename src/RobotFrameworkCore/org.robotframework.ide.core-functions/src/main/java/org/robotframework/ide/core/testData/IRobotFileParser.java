package org.robotframework.ide.core.testData;

import java.io.File;

import org.robotframework.ide.core.testData.model.IRobotFileOutput;


public interface IRobotFileParser {

    boolean canParseFile(final File file);


    void parse(final IRobotFileOutput output, final File robotFile);
}
