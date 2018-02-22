/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author mmarzec
 */
public class RobotDryRunLibraryImport {

    private final String name;

    private final URI sourcePath;

    private final DryRunLibraryType type;

    private final List<URI> importersPaths = newArrayList();

    private final List<String> args = newArrayList();

    private DryRunLibraryImportStatus status;

    private String additionalInfo;

    public RobotDryRunLibraryImport(final String name) {
        this(name, null, new ArrayList<String>());
    }

    public RobotDryRunLibraryImport(final String name, final URI sourcePath) {
        this(name, sourcePath, null, new ArrayList<>());
    }

    public RobotDryRunLibraryImport(final String name, final URI importerPath, final List<String> args) {
        this(name, null, importerPath, args);
    }

    public RobotDryRunLibraryImport(final String name, final URI sourcePath, final URI importerPath,
            final List<String> args) {
        this.name = name;
        this.sourcePath = resolveSourcePath(sourcePath);
        this.type = resolveType(this.name, this.sourcePath);
        if (importerPath != null) {
            this.importersPaths.add(importerPath);
        }
        this.args.addAll(args);
        this.status = DryRunLibraryImportStatus.ADDED;
        this.additionalInfo = "";
    }

    private URI resolveSourcePath(final URI sourcePath) {
        if (sourcePath == null) {
            return null;
        }

        try {
            final String path = sourcePath.getPath();
            if (path.endsWith(".pyc")) {
                return new URI("file", null, null, -1, path.substring(0, path.length() - 1), null, null);
            } else if (path.endsWith("$py.class")) {
                return new URI("file", null, null, -1, path.replace("$py.class", ".py"), null, null);
            }
            return sourcePath;
        } catch (final URISyntaxException e) {
            return sourcePath;
        }
    }

    private DryRunLibraryType resolveType(final String name, final URI sourcePath) {
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

    public String getName() {
        return name;
    }

    public URI getSourcePath() {
        return sourcePath;
    }

    public List<URI> getImportersPaths() {
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

    public void addImporterPath(final URI path) {
        if (path != null && !importersPaths.contains(path)) {
            importersPaths.add(path);
        }
    }

    public void setImportersPaths(final Collection<URI> importersPaths) {
        this.importersPaths.clear();
        this.importersPaths.addAll(importersPaths.stream().filter(uri -> uri != null).collect(toSet()));
        this.importersPaths.sort((uri1, uri2) -> uri1.compareTo(uri2));
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
