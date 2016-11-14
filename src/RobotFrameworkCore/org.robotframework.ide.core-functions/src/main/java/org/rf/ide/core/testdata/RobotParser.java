/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.testdata.importer.ResourceImporter;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;
import org.rf.ide.core.testdata.importer.VariablesImporter;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.TsvRobotFileParser;
import org.rf.ide.core.testdata.text.read.TxtRobotFileParser;

public class RobotParser {

    private static final int MAX_NUMBER_OF_TRASH_LINES = 5000;

    private static final List<IRobotFileParser> AVAIL_FORMAT_PARSERS = new ArrayList<>();

    static {
        AVAIL_FORMAT_PARSERS.add(new TxtRobotFileParser());
        AVAIL_FORMAT_PARSERS.add(new TsvRobotFileParser());
    }

    private final RobotParserConfig parserCfg;

    private final String robotVersionFromCommand;

    private final RobotVersion robotVersion;

    private final RobotProjectHolder robotProject;

    private final PathsProvider pathsProvider;

    /**
     * Creates parser which eagerly parses given file with all dependent
     * resources (resource files, variables)
     * 
     * @param projectHolder
     * @return eager parser
     */
    public static RobotParser createEager(final RobotProjectHolder projectHolder, final PathsProvider pathsProvider) {
        final RobotParserConfig cfg = new RobotParserConfig();
        cfg.setEagerImport(true);
        return create(projectHolder, cfg, pathsProvider);
    }

    /**
     * Creates parser which parses only given file without dependencies.
     * 
     * @param projectHolder
     * @return normal parser
     */
    public static RobotParser create(final RobotProjectHolder projectHolder, final PathsProvider pathsProvider) {
        return new RobotParser(projectHolder, new RobotParserConfig(), pathsProvider);
    }

    public static RobotParser create(final RobotProjectHolder projectHolder, final RobotParserConfig cfg,
            final PathsProvider pathsProvider) {
        return new RobotParser(projectHolder, cfg, pathsProvider);
    }

    public static RobotParser create(final RobotProjectHolder projectHolder, final RobotParserConfig cfg) {
        return new RobotParser(projectHolder, cfg, null);
    }

    private RobotParser(final RobotProjectHolder robotProject, final RobotParserConfig cfg,
            final PathsProvider pathsProvider) {
        this.robotProject = robotProject;
        this.pathsProvider = pathsProvider;
        this.robotVersionFromCommand = robotProject.getRobotRuntime() != null
                ? robotProject.getRobotRuntime().getVersion() : null;
        this.robotVersion = robotVersionFromCommand != null ? RobotVersion.from(robotVersionFromCommand) : null;
        this.parserCfg = cfg;
    }

    public final RobotVersion getRobotVersion() {
        return robotVersion;
    }

    public boolean isImportingEagerly() {
        return this.parserCfg.isEagerImportOn();
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
        final RobotFileOutput robotFile = new RobotFileOutput(robotVersion);

        final IRobotFileParser parserToUse = getParser(fileOrDir, true);

        if (parserToUse != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
            if (fileContent != null && fileContent.length() > 0) {
                bais = new ByteArrayInputStream(fileContent.getBytes(Charset.forName("UTF-8")));
            }

            parserToUse.parse(robotFile, bais, fileOrDir);

            final RobotFile fileModel = robotFile.getFileModel();
            if (fileModel.containsAnyRobotSection()) {
                final List<File> alreadyImported = new ArrayList<>();
                alreadyImported.add(fileOrDir);
                importExternal(robotFile);
            } else {
                if (fileModel.getFileContent().size() > MAX_NUMBER_OF_TRASH_LINES) {
                    fileModel.removeLines();
                }
            }
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
            final boolean isDir = fileOrDir.isDirectory();
            if (isDir) {
                final int currentOutputSize = output.size();
                final File[] files = fileOrDir.listFiles();
                for (final File f : files) {
                    parse(f, output);
                }

                if (currentOutputSize < output.size()) {
                    // is high level test suite
                    // TODO: place where in case of TH type we should put
                    // information
                }
            } else if (robotProject.shouldBeLoaded(fileOrDir)) {
                final IRobotFileParser parserToUse = getParser(fileOrDir, false);

                if (parserToUse != null) {
                    final RobotFileOutput robotFile = new RobotFileOutput(robotVersion);
                    output.add(robotFile);

                    // do not change order !!! for performance reason is better
                    // to execute importing of variables before add to model,
                    // which replace previous object
                    parserToUse.parse(robotFile, fileOrDir);
                    robotProject.addModelFile(robotFile);

                    final RobotFile fileModel = robotFile.getFileModel();
                    if (fileModel.containsAnyRobotSection()) {
                        importExternal(robotFile);
                    } else {
                        if (fileModel.getFileContent().size() > MAX_NUMBER_OF_TRASH_LINES) {
                            fileModel.removeLines();
                        }
                    }
                }
            } else {
                final RobotFileOutput fileByName = robotProject.findFileByName(fileOrDir);
                if (fileByName != null) {
                    output.add(fileByName);
                }
            }
        }
    }

    private void importExternal(final RobotFileOutput robotFile) {
        if (robotFile.getStatus() == Status.PASSED) {
            if (parserCfg.isEagerImportOn()) {
                // eager get resources example
                final ResourceImporter resImporter = new ResourceImporter();
                resImporter.importResources(this, robotFile);
            }

            if (parserCfg.shouldImportVariables()) {
                final VariablesImporter varImporter = new VariablesImporter();
                final List<VariablesFileImportReference> varsImported = varImporter
                        .importVariables(pathsProvider, robotProject, robotFile);
                robotFile.setVariablesImportReferences(varsImported);
            }
        }
    }

    private IRobotFileParser getParser(final File fileOrDir, final boolean isFromStringContent) {
        IRobotFileParser parserToUse = null;
        for (final IRobotFileParser parser : AVAIL_FORMAT_PARSERS) {
            synchronized (parser) {
                if (parser.canParseFile(fileOrDir, isFromStringContent)) {
                    parserToUse = parser.newInstance();
                    break;
                }
            }
        }
        return parserToUse;
    }

    public static class RobotParserConfig {

        public static RobotParserConfig allImportsEager() {
            final RobotParserConfig config = new RobotParserConfig();
            config.shouldEagerImport = true;
            config.shouldImportVariables = true;
            return config;
        }

        public static RobotParserConfig allImportsLazy() {
            final RobotParserConfig config = new RobotParserConfig();
            config.shouldEagerImport = false;
            config.shouldImportVariables = false;
            return config;
        }

        private boolean shouldEagerImport = false;

        private boolean shouldImportVariables = true;

        public void setEagerImport(final boolean shouldEagerImport) {
            this.shouldEagerImport = shouldEagerImport;
        }

        public void setIncludeImportVariables(final boolean shouldImportVariables) {
            this.shouldImportVariables = shouldImportVariables;
        }

        public boolean isEagerImportOn() {
            return this.shouldEagerImport;
        }

        public boolean shouldImportVariables() {
            return this.shouldImportVariables;
        }
    }
}
