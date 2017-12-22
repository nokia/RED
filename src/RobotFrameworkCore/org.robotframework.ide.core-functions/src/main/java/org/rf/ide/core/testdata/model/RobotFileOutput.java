/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.testdata.importer.ResourceImportReference;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;
import org.rf.ide.core.testdata.importer.VariablesImporter;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;

public class RobotFileOutput {

    public static final long FILE_NOT_EXIST_EPOCH = 0;

    private final RobotVersion robotVersion;

    private File processedFile;

    private final RobotFile fileModel;

    private long lastModificationEpoch = FILE_NOT_EXIST_EPOCH;

    private final List<ResourceImportReference> resourceReferences = new ArrayList<>();

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
        final String fileExtension = getFileExtension(processedFile);
        format = FileFormat.getByExtension(fileExtension);
        this.processedFile = processedFile;
        this.lastModificationEpoch = processedFile.lastModified();
    }

    private String getFileExtension(final File processedFile) {
        String fileExtension = null;
        if (processedFile != null) {
            final String filename = processedFile.getName();
            final int lastDot = filename.lastIndexOf('.');
            if (lastDot > -1) {
                fileExtension = filename.substring(lastDot + 1);
            }
        }
        return fileExtension;
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

    public void addResourceReferences(final List<ResourceImportReference> references) {
        for (final ResourceImportReference resourceImportReference : references) {
            addResourceReference(resourceImportReference);
        }
    }

    public void addResourceReference(final ResourceImportReference ref) {
        final int positionToSet = findResourceReferencePositionToReplace(ref);

        if (positionToSet == -1) {
            resourceReferences.add(ref);
        } else {
            resourceReferences.set(positionToSet, ref);
        }
    }

    public int findResourceReferencePositionToReplace(final ResourceImportReference ref) {
        int positionToSet = -1;

        final int numberOfReferences = resourceReferences.size();
        for (int i = 0; i < numberOfReferences; i++) {
            final ResourceImportReference reference = resourceReferences.get(i);
            final File file = reference.getReference().getProcessedFile();
            final File thisFile = ref.getReference().getProcessedFile();
            boolean isSameFile = false;
            try {
                if (Files.isSameFile(file.toPath(), thisFile.toPath())) {
                    isSameFile = true;
                }
            } catch (final IOException e) {
                if (file.toPath().normalize().toAbsolutePath().equals(thisFile.toPath().normalize().toAbsolutePath())) {
                    isSameFile = true;
                }
            }

            if (isSameFile) {
                positionToSet = i;
                break;
            }
        }

        return positionToSet;
    }

    public List<ResourceImportReference> getResourceImportReferences() {
        return Collections.unmodifiableList(resourceReferences);
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

    public static class BuildMessage {

        private final LogLevel type;

        private final String message;

        private String fileName;

        private FileRegion fileRegion;

        public BuildMessage(final LogLevel level, final String message, final String fileName) {
            this.type = level;
            this.message = message;
            this.fileName = fileName.intern();
        }

        public static BuildMessage createInfoMessage(final String message, final String fileName) {
            return new BuildMessage(LogLevel.INFO, message, fileName);
        }

        public static BuildMessage createWarnMessage(final String message, final String fileName) {
            return new BuildMessage(LogLevel.WARN, message, fileName);
        }

        public static BuildMessage createErrorMessage(final String message, final String fileName) {
            return new BuildMessage(LogLevel.ERROR, message, fileName);
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(final String fileName) {
            this.fileName = fileName;
        }

        public FileRegion getFileRegion() {
            return fileRegion;
        }

        public void setFileRegion(final FileRegion fileRegion) {
            this.fileRegion = fileRegion;
        }

        public LogLevel getType() {
            return type;
        }

        public String getMessage() {
            return message;
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
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
            result = prime * result + ((fileRegion == null) ? 0 : fileRegion.hashCode());
            result = prime * result + ((message == null) ? 0 : message.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final BuildMessage other = (BuildMessage) obj;
            if (fileName == null) {
                if (other.fileName != null)
                    return false;
            } else if (!fileName.equals(other.fileName))
                return false;
            if (fileRegion == null) {
                if (other.fileRegion != null)
                    return false;
            } else if (!fileRegion.equals(other.fileRegion))
                return false;
            if (message == null) {
                if (other.message != null)
                    return false;
            } else if (!message.equals(other.message))
                return false;
            if (type != other.type)
                return false;
            return true;
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public static enum Status {
        FAILED,
        PASSED
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

    public enum RobotFileType {
        UNKNOWN,
        RESOURCE,
        TEST_SUITE,
        TEST_SUITE_DIR,
        TEST_SUITE_INIT;
    }
}
