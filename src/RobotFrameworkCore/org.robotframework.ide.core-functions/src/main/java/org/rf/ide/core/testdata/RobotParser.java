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

    private final RobotVersion version;

    public RobotParser(final RobotProjectHolder robotProject, final RobotVersion version) {
        this.robotProject = robotProject;
        this.version = version;
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
        final RobotFileOutput robotFile = new RobotFileOutput(version);

        final TextualRobotFileParser parser = createParser(fileOrDir, true);

        if (parser != null) {
            final InputStream inputStream = fileContent == null || fileContent.isEmpty()
                    ? new ByteArrayInputStream(new byte[0])
                    : new ByteArrayInputStream(fileContent.getBytes(Charset.forName("UTF-8")));

            parser.parse(robotFile, inputStream, fileOrDir);

            clearIfNeeded(robotFile);
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
                final File[] files = fileOrDir.listFiles();
                if (files != null) {
                    for (final File file : files) {
                        parse(file, output);
                    }
                }
            } else if (robotProject.shouldBeLoaded(fileOrDir)) {
                final TextualRobotFileParser parser = createParser(fileOrDir, false);

                if (parser != null) {
                    final RobotFileOutput robotFile = new RobotFileOutput(version);
                    output.add(robotFile);

                    parser.parse(robotFile, fileOrDir);
                    robotProject.addModelFile(robotFile);

                    clearIfNeeded(robotFile);
                }
            } else {
                final RobotFileOutput fileByName = robotProject.findFileByName(fileOrDir);
                if (fileByName != null) {
                    output.add(fileByName);
                }
            }
        }
    }

    private TextualRobotFileParser createParser(final File fileOrDir, final boolean isFromStringContent) {
        if (fileOrDir == null || !fileOrDir.isFile() && !isFromStringContent) {
            return null;
        }
        return Arrays.stream(AVAILABLE_FORMATS)
                .filter(fileFormat -> FileFormat.getByFile(fileOrDir) == fileFormat)
                .findFirst()
                .map(TextualRobotFileParser::new)
                .orElse(null);
    }

    private void clearIfNeeded(final RobotFileOutput robotFile) {
        final RobotFile fileModel = robotFile.getFileModel();
        if (fileModel.getFileContent().size() > MAX_NUMBER_OF_TRASH_LINES) {
            fileModel.removeLines();
        }
    }
}
