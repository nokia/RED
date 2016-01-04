/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotProjectHolder;

public class RobotModelTestProvider {

    public static RobotModelTestProvider getInstance() {
        return new RobotModelTestProvider();
    }
    
    public static RobotParser getParser() {
        final RobotRuntimeEnvironment runEnv = mock(RobotRuntimeEnvironment.class);
        when(runEnv.getVersion()).thenReturn("3.0a1");
        final RobotProjectHolder robotProject = new RobotProjectHolder(runEnv);
        final RobotParser parser = RobotParser.createEager(robotProject);
        return parser;
    }

    public static RobotFile getModelFile(final String filename, final RobotParser parser) throws URISyntaxException {
        final Path path = Paths.get(getInstance().getClass().getResource(filename).toURI());
        return getModelFile(path, parser);
    }

    public static RobotFile getModelFile(final Path path, final RobotParser parser) {
        final List<RobotFileOutput> parsedFileList = parser.parse(path.toFile());
        final RobotFileOutput robotFileOutput = parsedFileList.get(0);
        return robotFileOutput.getFileModel();
    }
}
