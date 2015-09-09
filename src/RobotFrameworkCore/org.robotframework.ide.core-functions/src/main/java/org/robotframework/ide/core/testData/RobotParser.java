/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.importer.ResourceImporter;
import org.robotframework.ide.core.testData.importer.VariablesFileImportReference;
import org.robotframework.ide.core.testData.importer.VariablesImporter;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotFileOutput.Status;
import org.robotframework.ide.core.testData.model.RobotProjectHolder;
import org.robotframework.ide.core.testData.text.read.TxtRobotFileParser;


public class RobotParser {

    private boolean shouldEagerImport = false;
    private final RobotProjectHolder robotProject;

    private static final List<IRobotFileParser> availableFormatParsers = new LinkedList<>();
    static {
        availableFormatParsers.add(new TxtRobotFileParser());
    }


    public RobotParser(final RobotProjectHolder robotProject) {
        this.robotProject = robotProject;
    }


    public List<RobotFileOutput> parse(final File fileOrDir) {
        List<RobotFileOutput> output = new LinkedList<>();
        parse(fileOrDir, output);
        return output;
    }


    private void parse(final File fileOrDir, final List<RobotFileOutput> output) {
        if (fileOrDir != null) {
            boolean isDir = fileOrDir.isDirectory();
            if (isDir) {
                int currentOutputSize = output.size();
                File[] files = fileOrDir.listFiles();
                for (File f : files) {
                    parse(f, output);
                }

                if (currentOutputSize < output.size()) {
                    // is high level test suite
                    // TODO: place where in case of TH type we should put
                    // information
                }
            } else if (robotProject.shouldBeLoaded(fileOrDir)) {
                IRobotFileParser parserToUse = null;
                for (IRobotFileParser parser : availableFormatParsers) {
                    if (parser.canParseFile(fileOrDir)) {
                        parserToUse = parser;
                        break;
                    }
                }

                if (parserToUse != null) {
                    RobotFileOutput robotFile = new RobotFileOutput();

                    output.add(robotFile);
                    robotProject.addModelFile(robotFile);

                    parserToUse.parse(robotFile, fileOrDir);
                    if (robotFile.getStatus() == Status.PASSED) {
                        if (shouldEagerImport) {
                            // eager get resources example
                            ResourceImporter resImporter = new ResourceImporter();
                            resImporter.importResources(this, robotFile);
                        }

                        VariablesImporter varImporter = new VariablesImporter();
                        List<VariablesFileImportReference> varsImported = varImporter
                                .importVariables(
                                        robotProject.getRobotRuntime(),
                                        robotFile);
                        robotFile.addVariablesReferenced(varsImported);
                    }
                }
            }
        }
    }


    public void setEagerImport(final boolean shouldEagerImport) {
        this.shouldEagerImport = shouldEagerImport;
    }
}
