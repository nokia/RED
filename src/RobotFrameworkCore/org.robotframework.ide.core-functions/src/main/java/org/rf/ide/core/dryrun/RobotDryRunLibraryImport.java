/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author mmarzec
 *
 */
public class RobotDryRunLibraryImport {

    private final String name;

    private final String sourcePath;

    private String additionalInfo;

    private final List<String> importersPaths = newArrayList();

    private final List<String> args = newArrayList();

    private DryRunLibraryImportStatus status;

    private final DryRunLibraryType type;

    public RobotDryRunLibraryImport(final String name) {
        this(name, "", "", new ArrayList<String>());
    }

    public RobotDryRunLibraryImport(final String name, final String importerPath, final List<String> args) {
        this(name, "", importerPath, args);
    }

    public RobotDryRunLibraryImport(final String name, final String sourcePath, final String importerPath, final List<String> args) {
        this.name = name;
        this.sourcePath = resolveSourcePath(sourcePath);
        this.type = resolveType(this.sourcePath);
        if (importerPath != null && !importerPath.isEmpty() && !importersPaths.contains(importerPath)) {
            this.importersPaths.add(importerPath);
        }
        this.args.addAll(args);
    }

    private String resolveSourcePath(final String sourcePath) {
        if (!sourcePath.isEmpty()) {
            if (sourcePath.endsWith(".pyc")) {
                return sourcePath.substring(0, sourcePath.length() - 1);
            } else if (sourcePath.endsWith("$py.class")) {
                return sourcePath.replace("$py.class", ".py");
            }
        }
        return sourcePath;
    }

    private DryRunLibraryType resolveType(final String sourcePath) {
        if (!sourcePath.isEmpty()) {
            if (sourcePath.endsWith(".jar") || sourcePath.endsWith(".java") || sourcePath.endsWith(".class")) {
                return DryRunLibraryType.JAVA;
            } else {
                return DryRunLibraryType.PYTHON;
            }
        }
        return DryRunLibraryType.UNKNOWN;
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



