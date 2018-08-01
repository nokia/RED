/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;

public class RobotModelTestProvider {

    public static RobotModelTestProvider getInstance() {
        return new RobotModelTestProvider();
    }

    public static RobotParser getParser() {
        return getParser("3.0a1");
    }

    public static RobotParser getParser(final String version) {
        final RobotRuntimeEnvironment runEnv = mock(RobotRuntimeEnvironment.class);
        when(runEnv.getVersion()).thenReturn(version);
        final RobotProjectHolder robotProject = new RobotProjectHolder(runEnv);
        return RobotParser.create(robotProject, RobotParserConfig.allImportsEager(RobotVersion.from(version)));
    }

    public static RobotParser getLazyParser() {
        final RobotRuntimeEnvironment runEnv = mock(RobotRuntimeEnvironment.class);
        when(runEnv.getVersion()).thenReturn("3.0a1");
        final RobotProjectHolder robotProject = new RobotProjectHolder(runEnv);
        return RobotParser.create(robotProject, RobotParserConfig.allImportsLazy(new RobotVersion(3, 0)));
    }

    public static RobotParser getLazyParser(final String version) {
        final RobotRuntimeEnvironment runEnv = mock(RobotRuntimeEnvironment.class);
        when(runEnv.getVersion()).thenReturn(version);
        final RobotProjectHolder robotProject = new RobotProjectHolder(runEnv);
        return RobotParser.create(robotProject, RobotParserConfig.allImportsLazy(RobotVersion.from(version)));
    }

    public static RobotFile getModelFile(final String fileContent, final FileFormat format, final RobotParser parser) {
        return parser.parseEditorContent(fileContent, new File("dummy." + format.getExtension())).getFileModel();
    }

    public static RobotFile getModelFile(final String filename, final RobotParser parser) throws URISyntaxException {
        final Path path = getFilePath(filename);
        return getModelFile(path, parser);
    }

    public static Path getFilePath(final String filename) throws URISyntaxException {
        return Paths.get(getInstance().getClass().getResource(filename).toURI());
    }

    public static RobotFile getModelFile(final Path path, final RobotParser parser) {
        final List<RobotFileOutput> parsedFileList = parser.parse(path.toFile());
        final RobotFileOutput robotFileOutput = parsedFileList.get(0);
        return robotFileOutput.getFileModel();
    }
}
