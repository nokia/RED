/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.testdata.importer.ResourceImporter;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;
import org.rf.ide.core.testdata.importer.VariablesImporter;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.text.read.TextualRobotFileParser;

public class RobotParser {

    private static final int MAX_NUMBER_OF_TRASH_LINES = 5000;

    private static final FileFormat[] AVAILABLE_FORMATS = new FileFormat[] { FileFormat.TXT_OR_ROBOT, FileFormat.TSV };

    private final RobotProjectHolder robotProject;

    private final RobotParserConfig parserCfg;

    private final PathsProvider pathsProvider;

    public RobotParser(final RobotProjectHolder robotProject, final RobotParserConfig cfg) {
        this(robotProject, cfg, null);
    }

    public RobotParser(final RobotProjectHolder robotProject, final RobotParserConfig cfg,
            final PathsProvider pathsProvider) {
        this.robotProject = robotProject;
        this.parserCfg = cfg;
        this.pathsProvider = pathsProvider;
    }

    public final RobotVersion getRobotVersion() {
        return parserCfg.getVersion();
    }

    public boolean isImportingEagerly() {
        return parserCfg.isEagerImportOn();
    }

    /**
     * Should be used for unsaved editor content. Parsed output is not replacing
     * saved robot model in {@link RobotProjectHolder} object.
     *
     * @param fileContent
     * @param fileOrDir
     * @return
     */
    public RobotFileOutput parseEditorContent(final String fileContent, final File fileOrDir) {
        final RobotFileOutput robotFile = new RobotFileOutput(getRobotVersion());

        final TextualRobotFileParser parserToUse = getParser(fileOrDir, true);

        if (parserToUse != null) {
            final InputStream inputStream = fileContent == null || fileContent.isEmpty()
                    ? new ByteArrayInputStream(new byte[0])
                    : new ByteArrayInputStream(fileContent.getBytes(Charset.forName("UTF-8")));

            parserToUse.parse(robotFile, inputStream, fileOrDir);

            applyPostParsingActions(robotFile);
        } else {
            robotFile.addBuildMessage(
                    BuildMessage.createErrorMessage("No parser found for file.", fileOrDir.getAbsolutePath()));
            robotFile.setStatus(Status.FAILED);
        }

        return robotFile;
    }

    public List<RobotFileOutput> parse(final File fileOrDir) {
        final List<RobotFileOutput> output = new ArrayList<>();
        parse(fileOrDir, output);
        return output;
    }

    private void parse(final File fileOrDir, final List<RobotFileOutput> output) {
        if (fileOrDir != null) {
            if (fileOrDir.isDirectory()) {
                for (final File f : fileOrDir.listFiles()) {
                    parse(f, output);
                }
            } else if (robotProject.shouldBeLoaded(fileOrDir)) {
                final TextualRobotFileParser parserToUse = getParser(fileOrDir, false);

                if (parserToUse != null) {
                    final RobotFileOutput robotFile = new RobotFileOutput(getRobotVersion());
                    output.add(robotFile);

                    // do not change order !!! for performance reason is better
                    // to execute importing of variables before add to model,
                    // which replace previous object
                    parserToUse.parse(robotFile, fileOrDir);
                    robotProject.addModelFile(robotFile);

                    applyPostParsingActions(robotFile);
                }
            } else {
                final RobotFileOutput fileByName = robotProject.findFileByName(fileOrDir);
                if (fileByName != null) {
                    output.add(fileByName);
                }
            }
        }
    }

    private void applyPostParsingActions(final RobotFileOutput robotFile) {
        final RobotFile fileModel = robotFile.getFileModel();
        if (fileModel.containsAnyRobotSection()) {
            importExternal(robotFile);
        } else if (fileModel.getFileContent().size() > MAX_NUMBER_OF_TRASH_LINES) {
            fileModel.removeLines();
        }
    }

    private void importExternal(final RobotFileOutput robotFile) {
        if (robotFile.getStatus() == Status.PASSED) {
            if (parserCfg.isEagerImportOn()) {
                // eager get resources example
                final ResourceImporter resImporter = new ResourceImporter(this);
                resImporter.importResources(pathsProvider, robotProject, robotFile);
            }

            if (parserCfg.shouldImportVariables()) {
                final VariablesImporter varImporter = new VariablesImporter();
                final List<VariablesFileImportReference> varsImported = varImporter
                        .importVariables(pathsProvider, robotProject, robotFile);
                robotFile.setVariablesImportReferences(varsImported);
            }
        }
    }

    private TextualRobotFileParser getParser(final File fileOrDir, final boolean isFromStringContent) {
        if (fileOrDir == null || !fileOrDir.isFile() && !isFromStringContent) {
            return null;
        }
        return Arrays.stream(AVAILABLE_FORMATS)
                .filter(fileFormat -> FileFormat.getByFile(fileOrDir) == fileFormat)
                .findFirst()
                .map(TextualRobotFileParser::new)
                .orElse(null);
    }

    public static class RobotParserConfig {

        public static RobotParserConfig allImportsEager(final RobotVersion version) {
            final RobotParserConfig config = new RobotParserConfig(version);
            config.shouldEagerImport = true;
            config.shouldImportVariables = true;
            return config;
        }

        public static RobotParserConfig allImportsLazy(final RobotVersion version) {
            final RobotParserConfig config = new RobotParserConfig(version);
            config.shouldEagerImport = false;
            config.shouldImportVariables = false;
            return config;
        }

        private final RobotVersion version;

        private boolean shouldEagerImport = false;

        private boolean shouldImportVariables = true;

        public RobotParserConfig(final RobotVersion version) {
            this.version = version;
        }

        public RobotVersion getVersion() {
            return version;
        }

        public boolean isEagerImportOn() {
            return shouldEagerImport;
        }

        public boolean shouldImportVariables() {
            return shouldImportVariables;
        }
    }
}
