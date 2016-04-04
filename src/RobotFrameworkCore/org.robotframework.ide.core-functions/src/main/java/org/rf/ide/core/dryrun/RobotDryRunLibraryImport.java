/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Objects;

/**
 * @author mmarzec
 *
 */
public class RobotDryRunLibraryImport {

    private String name;

    private String sourcePath;

    private String additionalInfo;

    private List<String> importersPaths = newArrayList();
    
    private List<String> args = newArrayList();

    private DryRunLibraryImportStatus status;

    private DryRunLibraryType type;

    public RobotDryRunLibraryImport(final String name, final List<String> args) {
        this(name, "", "", args);
    }

    public RobotDryRunLibraryImport(final String name, final String sourcePath, final String importerPath, final List<String> args) {
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
            final RobotDryRunLibraryImport other = (RobotDryRunLibraryImport) obj;
            return Objects.equals(name, other.name) && Objects.equals(sourcePath, other.sourcePath)
                    && Objects.equals(type, other.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sourcePath, type);
    }
    
    public static enum DryRunLibraryImportStatus {
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

    public static enum DryRunLibraryType {
        PYTHON,
        JAVA,
        UNKNOWN
    }
}



