/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.io.Files;

/**
 * @author mmarzec
 */
public class RobotDryRunOutputParser implements ILineHandler {

    private static final String MESSAGE_EVENT_NAME = "message";

    private static final String LIBRARY_IMPORT_EVENT_NAME = "library_import";

    private final ObjectMapper mapper;

    private Map<String, Object> parsedLine;

    private List<DryRunLibraryImport> importedLibraries = new LinkedList<>();

    private Set<String> standardLibrariesNames;
    
    private DryRunLibraryImport currentLibraryImportWithFail;

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
            final Map<String, Object> details = (Map<String, Object>) libraryImportList.get(1);
            final String libraryName = (String) libraryImportList.get(0);
            final String importer = (String) details.get("importer");
            final String source = (String) details.get("source");
            final List<String> args = (List<String>) details.get("args");
            if (importer != null) {
                DryRunLibraryImport dryRunLibraryImport = null;
                if (source != null) {
                    if (currentLibraryImportWithFail != null
                            && libraryName.equals(currentLibraryImportWithFail.getName())) {
                        dryRunLibraryImport = currentLibraryImportWithFail;
                    } else {
                        dryRunLibraryImport = new DryRunLibraryImport(libraryName, source, importer, args);
                    }
                } else {
                    dryRunLibraryImport = new DryRunLibraryImport(libraryName, args);
                }
                int index = importedLibraries.indexOf(dryRunLibraryImport);
                if (index < 0) {
                    if (!standardLibrariesNames.contains(libraryName)) {
                        importedLibraries.add(dryRunLibraryImport);
                    }
                } else {
                    importedLibraries.get(index).addImporterPath(importer);
                }
            }
            resetCurrentLibraryImportWithFail();
        }
        if (parsedLine.containsKey(MESSAGE_EVENT_NAME)) {
            final List<Object> messageList = (List<Object>) parsedLine.get(MESSAGE_EVENT_NAME);
            final Map<String, String> details = (Map<String, String>) messageList.get(0);
            final String messageLevel = details.get("level");
            if (messageLevel != null && messageLevel.equalsIgnoreCase("FAIL")) {
                String message = details.get("message");
                if (message != null) {
                    String libraryName = extractLibName(message);
                    if (!libraryName.isEmpty()) {
                        resetCurrentLibraryImportWithFail();
                        final DryRunLibraryImport dryRunLibraryImport = new DryRunLibraryImport(libraryName,
                                new ArrayList<String>());
                        int libIndex = importedLibraries.indexOf(dryRunLibraryImport);
                        final String failReason = extractFailReason(message);
                        if (libIndex < 0) {
                            dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                                    failReason);
                            importedLibraries.add(dryRunLibraryImport);
                        } else {
                            importedLibraries.get(libIndex)
                                    .setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED, failReason);
                        }
                        currentLibraryImportWithFail = dryRunLibraryImport;
                    }
                }
            }
        }
    }
    
    private void resetCurrentLibraryImportWithFail() {
        currentLibraryImportWithFail = null;
    }

    private String extractLibName(final String message) {
        final String libName = extractElement(message, "LIB_ERROR:");
        if (Files.isFile().apply(new File(libName))) {
            return Files.getNameWithoutExtension(libName);
        }
        return libName;
    }

    private String extractElement(final String message, final String key) {
        int keyIndex = message.indexOf(key);
        if (keyIndex >= 0) {
            int endIndex = message.indexOf(",", keyIndex);
            if (endIndex >= 0 && endIndex > keyIndex) {
                return message.substring(keyIndex + key.length(), endIndex).trim();
            }
        }
        return "";
    }

    private String extractFailReason(final String message) {
        if (message != null) {
            String failReason = message.replaceAll("\\\\n", "\n").replaceAll("\\\\'", "'");
            final String startText = "VALUE_START";
            final String endText = "VALUE_END";
            int beginIndex = failReason.indexOf(startText);
            int endIndex = failReason.lastIndexOf(endText);
            if (beginIndex >= 0 && endIndex > 0) {
                return failReason.substring(beginIndex + startText.length() + 1, endIndex)
                        .replace("<class 'robot.errors.DataError'>, DataError(", "");
            }
            return failReason;
        }
        return "";
    }

    public List<DryRunLibraryImport> getImportedLibraries() {
        return importedLibraries;
    }

    public static class DryRunLibraryImport {

        private String name;

        private String sourcePath;

        private String additionalInfo;

        private List<String> importersPaths = newArrayList();
        
        private List<String> args = newArrayList();

        private DryRunLibraryImportStatus status;

        private DryRunLibraryType type;

        public DryRunLibraryImport(final String name, final List<String> args) {
            this(name, "", "", args);
        }

        public DryRunLibraryImport(final String name, final String sourcePath, final String importerPath, final List<String> args) {
            this.name = name;
            this.sourcePath = checkLibSourcePathAndType(sourcePath);
            if (importerPath != null && !importerPath.isEmpty() && !importersPaths.contains(importerPath)) {
                this.importersPaths.add(importerPath);
            }
            this.args.addAll(args);
        }

        private String checkLibSourcePathAndType(final String sourcePath) {
            String sourcePathCheckResult = "";
            if (sourcePath == null || sourcePath.isEmpty()) {
                type = DryRunLibraryType.UNKNOWN;
            } else {
                sourcePathCheckResult = sourcePath;
                if (sourcePath.endsWith(".pyc")) {
                    sourcePathCheckResult = sourcePath.substring(0, sourcePath.length() - 1);
                } else if (sourcePath.endsWith("$py.class")) {
                    sourcePathCheckResult = sourcePath.replace("$py.class", ".py");
                }
                
                if (sourcePathCheckResult.endsWith(".jar") || sourcePathCheckResult.endsWith(".java")
                        || sourcePathCheckResult.endsWith(".class")) {
                    type = DryRunLibraryType.JAVA;
                } else {
                    type = DryRunLibraryType.PYTHON;
                }
            }
            return sourcePathCheckResult;
        }

        public String getName() {
            return name;
        }

        public String getSourcePath() {
            return sourcePath;
        }

        public void setSourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
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
        
        public void addImporterPath(final String path) {
            if(!importersPaths.contains(path)) {
                importersPaths.add(path);
            }
        }

        public List<String> getArgs() {
            return args;
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
        JAVA,
        UNKNOWN
    }
}
