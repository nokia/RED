/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;
import org.rf.ide.core.testdata.importer.VariablesImporter;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;

public class RobotFileOutput {

    public static final long FILE_NOT_EXIST_EPOCH = 0;

    private final RobotVersion robotVersion;

    private File processedFile;

    private final RobotFile fileModel;

    private long lastModificationEpoch = FILE_NOT_EXIST_EPOCH;

    private List<VariablesFileImportReference> variablesReferenced = null;

    private final List<BuildMessage> buildingMessages = new ArrayList<>();

    private Status status = Status.FAILED;

    private FileFormat format = FileFormat.UNKNOWN;

    private final FileRegionCacher<IDocumentationHolder> docCacher;

    public RobotFileOutput(final RobotVersion robotVersion) {
        this.robotVersion = robotVersion;
        this.fileModel = new RobotFile(this);
        this.docCacher = new FileRegionCacher<>();
    }

    public String getFileLineSeparator() {
        String result = "";

        final List<RobotLine> fileContent = fileModel.getFileContent();
        if (!fileContent.isEmpty()) {
            final IRobotLineElement endOfLine = fileContent.get(0).getEndOfLine();
            final List<IRobotTokenType> types = endOfLine.getTypes();
            if (!types.isEmpty()) {
                final IRobotTokenType eolType = types.get(0);
                final List<String> representation = eolType.getRepresentation();
                if (!representation.isEmpty()) {
                    result = representation.get(0);
                }
            }
        }

        return result;
    }

    public Optional<IDocumentationHolder> findDocumentation(final int offset, final int line) {
        final List<IRegionCacheable<IDocumentationHolder>> docInOffset = docCacher.findByOffset(offset);
        final List<IRegionCacheable<IDocumentationHolder>> docInLine = docCacher.findByLineNumber(line);

        if (!docInLine.isEmpty()) {
            if (docInOffset.size() < docInLine.size() || docInOffset.size() == docInLine.size()) {
                return Optional.ofNullable(docInLine.get(0).getCached());
            }
        }

        return Optional.empty();
    }

    public Optional<IDocumentationHolder> findDocumentationForOffset(final int offset) {
        final List<IRegionCacheable<IDocumentationHolder>> found = docCacher.findByOffset(offset);
        if (found.size() >= 1) {
            return Optional.of(found.get(0).getCached());
        }

        return Optional.empty();
    }

    public Optional<IDocumentationHolder> findDocumentationForLine(final int lineNumber) {
        final List<IRegionCacheable<IDocumentationHolder>> found = docCacher.findByLineNumber(lineNumber);
        if (found.size() >= 1) {
            return Optional.of(found.get(0).getCached());
        }

        return Optional.empty();
    }

    public FileRegionCacher<IDocumentationHolder> getDocumentationCacher() {
        return this.docCacher;
    }

    public RobotVersion getRobotVersion() {
        return robotVersion;
    }

    public File getProcessedFile() {
        return processedFile;
    }

    public FileFormat getFileFormat() {
        return format;
    }

    public void setProcessedFile(final File processedFile) {
        this.format = FileFormat.getByFile(processedFile);
        this.processedFile = processedFile;
        this.lastModificationEpoch = processedFile.lastModified();
    }

    public void setLastModificationEpochTime(final long lastModificationEpoch) {
        this.lastModificationEpoch = lastModificationEpoch;
    }

    public long getLastModificationEpochTime() {
        return lastModificationEpoch;
    }

    public RobotFile getFileModel() {
        return fileModel;
    }

    public List<BuildMessage> getBuildingMessages() {
        return Collections.unmodifiableList(buildingMessages);
    }

    public void addBuildMessage(final BuildMessage msg) {
        buildingMessages.add(msg);
    }

    public void setVariablesImportReferences(final List<VariablesFileImportReference> varReferences) {
        this.variablesReferenced = varReferences;
    }

    public List<VariablesFileImportReference> getVariablesImportReferences(final RobotProjectHolder robotProject,
            final PathsProvider pathsProvider) {
        if (variablesReferenced == null) {
            variablesReferenced = new ArrayList<>();

            final List<VariablesFileImportReference> varsImported = new VariablesImporter()
                    .importVariables(pathsProvider, robotProject, this);
            variablesReferenced.addAll(varsImported);
        }
        return Collections.unmodifiableList(variablesReferenced);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public RobotFileType getType() {
        if (fileModel != null) {
            if (processedFile.isFile()) {
                if (RobotFile.INIT_NAMES.stream().anyMatch(processedFile.getName()::equalsIgnoreCase)) {
                    return RobotFileType.TEST_SUITE_INIT;
                } else {
                    if (fileModel.getTestCaseTable().isPresent()) {
                        return RobotFileType.TEST_SUITE;
                    } else if (fileModel.containsAnyRobotSection()) {
                        return RobotFileType.RESOURCE;
                    }
                }
            } else {
                return RobotFileType.TEST_SUITE_DIR;
            }
        }

        return RobotFileType.UNKNOWN;
    }

    public static enum Status {
        FAILED,
        PASSED
    }

    public enum RobotFileType {
        UNKNOWN,
        RESOURCE,
        TEST_SUITE,
        TEST_SUITE_DIR,
        TEST_SUITE_INIT;
    }

    public static class BuildMessage {

        public static BuildMessage createWarnMessage(final String message, final String fileName,
                final FileRegion fileRegion) {
            return new BuildMessage(LogLevel.WARN, message, fileName, fileRegion);
        }

        public static BuildMessage createErrorMessage(final String message, final String fileName) {
            return createErrorMessage(message, fileName, null);
        }

        public static BuildMessage createErrorMessage(final String message, final String fileName,
                final FileRegion fileRegion) {
            return new BuildMessage(LogLevel.ERROR, message, fileName, fileRegion);
        }

        private final LogLevel type;

        private final String message;

        private final String fileName;

        private final FileRegion fileRegion;

        private BuildMessage(final LogLevel level, final String message, final String fileName,
                final FileRegion fileRegion) {
            this.type = level;
            this.message = message;
            this.fileName = fileName == null ? null : fileName.intern();
            this.fileRegion = fileRegion;
        }

        public LogLevel getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public String getFileName() {
            return fileName;
        }

        public FileRegion getFileRegion() {
            return fileRegion;
        }

        public static enum LogLevel {
            INFO,
            WARN,
            ERROR;
        }

        @Override
        public String toString() {
            return String.format("BuildMessage [type=%s, message=%s, fileName=%s, fileRegion=%s]", type, message,
                    fileName, fileRegion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, message, fileName, fileRegion);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == BuildMessage.class) {
                final BuildMessage that = (BuildMessage) obj;
                return this.type == that.type && Objects.equals(this.message, that.message)
                        && Objects.equals(this.fileName, that.fileName)
                        && Objects.equals(this.fileRegion, that.fileRegion);
            }
            return false;
        }
    }
}
