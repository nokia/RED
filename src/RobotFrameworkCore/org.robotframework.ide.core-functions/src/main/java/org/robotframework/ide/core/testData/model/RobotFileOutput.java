package org.robotframework.ide.core.testData.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


public class RobotFileOutput {

    private File processedFile;
    private RobotFile fileModel;
    private List<BuildMessage> buildingMessages = new LinkedList<>();
    private Status status = Status.FAILED;


    public File getProcessedFile() {
        return processedFile;
    }


    public void setProcessedFile(File processedFile) {
        this.processedFile = processedFile;
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
    }

    public static enum Status {
        FAILED, PASSED
    }
}
