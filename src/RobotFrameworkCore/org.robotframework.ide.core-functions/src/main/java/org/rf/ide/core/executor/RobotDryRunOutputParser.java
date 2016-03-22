/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author mmarzec
 *
 */
public class RobotDryRunOutputParser implements ILineHandler {

    private static final String MESSAGE_EVENT_NAME = "message";

    private static final String LIBRARY_IMPORT_EVENT_NAME = "library_import";

    private final ObjectMapper mapper;

    private Map<String, Object> parsedLine;

    private List<DryRunLibraryImport> importedLibraries = new LinkedList<>();

    private List<DryRunErrorMessage> errorMessages = newArrayList();

    private Set<String> standardLibrariesNames;

    public RobotDryRunOutputParser(final Set<String> standardLibrariesNames) {
        this.standardLibrariesNames = standardLibrariesNames;
        this.mapper = new ObjectMapper();
        this.parsedLine = new HashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processLine(final String line) {
        try {
            parsedLine = mapper.readValue(line, Map.class);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (parsedLine.containsKey(LIBRARY_IMPORT_EVENT_NAME)) {
            final List<Object> libraryImportList = (List<Object>) parsedLine.get(LIBRARY_IMPORT_EVENT_NAME);
            final Map<String, String> details = (Map<String, String>) libraryImportList.get(1);
            final String libraryName = (String) libraryImportList.get(0);
            final String importer = details.get("importer");
            if (importer != null) {
                final DryRunLibraryImport dryRunLibraryImport = new DryRunLibraryImport(libraryName,
                        details.get("source"), importer);
                int index = importedLibraries.indexOf(dryRunLibraryImport);
                if (index < 0) {
                    if (!standardLibrariesNames.contains(libraryName)) {
                        importedLibraries.add(dryRunLibraryImport);
                    }
                } else {
                    importedLibraries.get(index).getImportersPaths().add(importer);
                }
            }
        }
        if (parsedLine.containsKey(MESSAGE_EVENT_NAME)) {
            final List<Object> messageList = (List<Object>) parsedLine.get(MESSAGE_EVENT_NAME);
            final Map<String, String> details = (Map<String, String>) messageList.get(0);
            final String messageLevel = details.get("level");
            if (messageLevel != null && messageLevel.equalsIgnoreCase("ERROR")) {
                errorMessages.add(new DryRunErrorMessage(details.get("message")));
            }
        }
    }

    public List<DryRunLibraryImport> getImportedLibraries() {
        return importedLibraries;
    }

    public List<DryRunErrorMessage> getErrorMessages() {
        return errorMessages;
    }

    public static class DryRunLibraryImport {

        private String name;

        private String sourcePath;

        private String additionalInfo;
        
        private List<String> importersPaths = newArrayList();

        private DryRunLibraryImportStatus status;

        private DryRunLibraryType type;

        public DryRunLibraryImport(final String name, final String sourcePath, final String importerPath) {
            this.name = name;
            this.sourcePath = checkLibSourcePathAndType(sourcePath);
            if (importerPath != null) {
                this.importersPaths.add(importerPath);
            }
        }

        private String checkLibSourcePathAndType(final String sourcePath) {
            if (sourcePath.endsWith(".pyc")) {
                return sourcePath.substring(0, sourcePath.length() - 1);
            } else if (sourcePath.endsWith("$py.class")) {
                return sourcePath.replace("$py.class", ".py");
            }

            if (sourcePath.endsWith(".jar") || sourcePath.endsWith(".java")) {
                type = DryRunLibraryType.JAVA;
            } else {
                type = DryRunLibraryType.PYTHON;
            }

            return sourcePath;
        }

        public String getName() {
            return name;
        }

        public String getSourcePath() {
            return sourcePath;
        }

        public List<String> getImportersPaths() {
            return importersPaths;
        }

        public DryRunLibraryImportStatus getStatus() {
            return status;
        }

        public DryRunLibraryType getType() {
            return type;
        }

        public String getAdditionalInfo() {
            return additionalInfo;
        }

        public void setStatusAndAdditionalInfo(final DryRunLibraryImportStatus status, final String additionalInfo) {
            this.status = status;
            this.additionalInfo = additionalInfo;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            } else if (obj.getClass() == getClass()) {
                final DryRunLibraryImport other = (DryRunLibraryImport) obj;
                return Objects.equals(name, other.name) && Objects.equals(sourcePath, other.sourcePath)
                        && Objects.equals(type, other.type);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, sourcePath, type);
        }
    }

    public static class DryRunErrorMessage {

        private String message;

        public DryRunErrorMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

    }

    public enum DryRunLibraryImportStatus {
        ADDED("Added to project configuration"),
        ALREADY_EXISTING("Already existing in project configuration"),
        NOT_ADDED("Not added to project configuration");

        private String message;

        private DryRunLibraryImportStatus(final String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public enum DryRunLibraryType {
        PYTHON,
        JAVA
    }
}
