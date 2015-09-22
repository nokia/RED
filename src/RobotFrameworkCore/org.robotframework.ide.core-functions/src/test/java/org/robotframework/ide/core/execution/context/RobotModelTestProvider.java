/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution.context;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.robotframework.ide.core.testData.RobotParser;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotProjectHolder;

public class RobotModelTestProvider {

    public static RobotModelTestProvider getInstance() {
        return new RobotModelTestProvider();
    }

    public static RobotFile getModelFile(final String filename) throws URISyntaxException {
        final RobotProjectHolder robotProject = new RobotProjectHolder();
        final RobotParser parser = RobotParser.createEager(robotProject);

        final Path path = Paths.get(getInstance().getClass().getResource(filename).toURI());
        final List<RobotFileOutput> parsedFileList = parser.parse(path.toFile());
        final RobotFileOutput robotFileOutput = parsedFileList.get(0);
        return robotFileOutput.getFileModel();
    }
}
