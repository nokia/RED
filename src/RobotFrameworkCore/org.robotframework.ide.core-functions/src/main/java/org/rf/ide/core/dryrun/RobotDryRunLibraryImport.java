/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author mmarzec
 */
public class RobotDryRunLibraryImport {

    private final String name;

    private final URI sourcePath;

    private final DryRunLibraryType type;

    private final Set<URI> importersPaths;

    private final List<String> args;

    private DryRunLibraryImportStatus status;

    private String additionalInfo;

    public static RobotDryRunLibraryImport createUnknown(final String name) {
        return createUnknown(name, "");
    }

    public static RobotDryRunLibraryImport createUnknown(final String name, final String additionalInfo) {
        return new RobotDryRunLibraryImport(name, null, DryRunLibraryType.UNKNOWN, new HashSet<>(), new ArrayList<>(),
                DryRunLibraryImportStatus.NOT_ADDED, additionalInfo);
    }

    public static RobotDryRunLibraryImport createKnown(final String name, final URI originalPath) {
        return createKnown(name, originalPath, new HashSet<>(), new ArrayList<>());
    }

    public static RobotDryRunLibraryImport createKnown(final String name, final URI originalPath,
            final Set<URI> importersPaths, final List<String> args) {
        final URI sourcePath = resolveSourcePath(originalPath);
        final DryRunLibraryType type = resolveType(name, sourcePath);
        return new RobotDryRunLibraryImport(name, sourcePath, type, importersPaths, args,
                DryRunLibraryImportStatus.ADDED, "");
    }

    private static URI resolveSourcePath(final URI originalPath) {
        if (originalPath == null) {
            return null;
        }

        try {
            final String path = originalPath.getPath();
            if (path.endsWith(".pyc")) {
                final String normalizedPath = path.substring(0, path.length() - 1);
                return new URI("file", null, null, -1, normalizedPath, null, null);
            } else if (path.endsWith("$py.class")) {
                final String normalizedPath = path.replace("$py.class", ".py");
                return new URI("file", null, null, -1, normalizedPath, null, null);
            } else if (path.endsWith(".py") && path.contains(".jar/")) {
                final String normalizedPath = path.substring(0, path.lastIndexOf(".jar/")) + ".jar";
                return new URI("file", null, null, -1, normalizedPath, null, null);
            }
            return originalPath;
        } catch (final URISyntaxException e) {
            return originalPath;
        }
    }

    private static DryRunLibraryType resolveType(final String name, final URI sourcePath) {
        if (name.equals("Remote") || name.startsWith("Remote ")) {
            return DryRunLibraryType.REMOTE;
        } else {
            if (sourcePath != null) {
                final String path = sourcePath.getPath();
                if (path.endsWith(".jar") || path.endsWith(".java") || path.endsWith(".class")) {
                    return DryRunLibraryType.JAVA;
                } else {
                    return DryRunLibraryType.PYTHON;
                }
            }
        }
        return DryRunLibraryType.UNKNOWN;
    }

    @VisibleForTesting
    RobotDryRunLibraryImport(final String name, final URI sourcePath, final DryRunLibraryType type,
            final Set<URI> importersPaths, final List<String> args, final DryRunLibraryImportStatus status,
            final String additionalInfo) {
        this.name = name;
        this.sourcePath = sourcePath;
        this.type = type;
        this.importersPaths = importersPaths;
        this.args = args;
        this.status = status;
        this.additionalInfo = additionalInfo;
    }

    public String getName() {
        return name;
    }

    public URI getSourcePath() {
        return sourcePath;
    }

    public Set<URI> getImportersPaths() {
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

    public void setStatus(final DryRunLibraryImportStatus status) {
        this.status = status;
    }

    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public void setImportersPaths(final Set<URI> importersPaths) {
        this.importersPaths.clear();
        this.importersPaths.addAll(importersPaths);
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

    @Override
    public String toString() {
        return "RobotDryRunLibraryImport [name=" + name + ", sourcePath=" + sourcePath + ", type=" + type
                + ", importersPaths=" + importersPaths + ", args=" + args + ", status=" + status + ", additionalInfo="
                + additionalInfo + "]";
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
        UNKNOWN,
        REMOTE
    }
}
