package org.robotframework.ide.core.testData.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.importer.ResourceImportReference;
import org.robotframework.ide.core.testData.importer.VariablesFileImportReference;


public class RobotFileOutput {

    public static final long FILE_NOT_EXIST_EPOCH = 0;
    private File processedFile;
    private RobotFile fileModel;
    private long lastModificationEpoch = FILE_NOT_EXIST_EPOCH;
    private List<ResourceImportReference> resourceReferences = new LinkedList<>();
    private List<VariablesFileImportReference> variablesReferenced = new LinkedList<>();
    private List<BuildMessage> buildingMessages = new LinkedList<>();
    private Status status = Status.FAILED;


    public File getProcessedFile() {
        return processedFile;
    }


    public void setProcessedFile(File processedFile) {
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
        return buildingMessages;
    }


    public void addBuildMessage(final BuildMessage msg) {
        buildingMessages.add(msg);
    }


    public void setFileModel(RobotFile fileModel) {
        this.fileModel = fileModel;
    }


    public void setResourceReferences(
            final List<ResourceImportReference> references) {
        this.resourceReferences = references;
    }


    public List<ResourceImportReference> getResourceImportReferences() {
        return resourceReferences;
    }


    public void setVariablesReferenced(
            final List<VariablesFileImportReference> varsImported) {
        this.variablesReferenced = varsImported;
    }


    public List<VariablesFileImportReference> getVariablesImportReferences() {
        return variablesReferenced;
    }

    public static class BuildMessage {

        private final LogLevel type;
        private final String message;
        private String fileName;
        private FileRegion fileRegion;


        public BuildMessage(final LogLevel level, final String message,
                final String fileName) {
            this.type = level;
            this.message = message;
            this.fileName = fileName;
        }


        public static BuildMessage createInfoMessage(final String message,
                final String fileName) {
            return new BuildMessage(LogLevel.INFO, message, fileName);
        }


        public static BuildMessage createWarnMessage(final String message,
                final String fileName) {
            return new BuildMessage(LogLevel.WARN, message, fileName);
        }


        public static BuildMessage createErrorMessage(final String message,
                final String fileName) {
            return new BuildMessage(LogLevel.ERROR, message, fileName);
        }


        public String getFileName() {
            return fileName;
        }


        public void setFileName(String fileName) {
            this.fileName = fileName;
        }


        public FileRegion getFileRegion() {
            return fileRegion;
        }


        public void setFileRegion(FileRegion fileRegion) {
            this.fileRegion = fileRegion;
        }


        public LogLevel getType() {
            return type;
        }


        public String getMessage() {
            return message;
        }

        public static enum LogLevel {
            INFO, WARN, ERROR;
        }


        @Override
        public String toString() {
            return String
                    .format("BuildMessage [type=%s, message=%s, fileName=%s, fileRegion=%s]",
                            type, message, fileName, fileRegion);
        }
    }


    public Status getStatus() {
        return status;
    }


    public void setStatus(Status status) {
        this.status = status;
    }

    public static enum Status {
        FAILED, PASSED
    }


    public RobotFileType getType() {
        RobotFileType judgedType = RobotFileType.UNKNOWN;
        if (fileModel != null) {
            if (processedFile.isFile()) {
                String name = processedFile.getName().toLowerCase();
                if (name.startsWith("__init__")) {
                    judgedType = RobotFileType.TEST_SUITE_INIT;
                } else {
                    if (fileModel.getTestCaseTable().isPresent()) {
                        judgedType = RobotFileType.TEST_SUITE;
                    } else if (fileModel.containsAnyRobotSection()) {
                        judgedType = RobotFileType.RESOURCE;
                    } else {
                        judgedType = RobotFileType.UNKNOWN;
                    }
                }
            } else {
                judgedType = RobotFileType.TEST_SUITE_DIR;
            }
        }

        return judgedType;
    }

    public enum RobotFileType {
        UNKNOWN, RESOURCE, TEST_SUITE, TEST_SUITE_DIR, TEST_SUITE_INIT;
    }
}
