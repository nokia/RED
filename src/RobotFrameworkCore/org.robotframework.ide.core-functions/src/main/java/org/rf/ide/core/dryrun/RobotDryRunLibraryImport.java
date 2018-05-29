/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.net.URI;
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

    private final URI source;

    private final DryRunLibraryType type;

    private final Set<URI> importers;

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

    public static RobotDryRunLibraryImport createKnown(final String name, final URI source, final Set<URI> importers,
            final List<String> args) {
        final DryRunLibraryType type = resolveType(name, source);
        return new RobotDryRunLibraryImport(name, source, type, importers, args, DryRunLibraryImportStatus.ADDED, "");
    }

    private static DryRunLibraryType resolveType(final String name, final URI source) {
        if (name.equals("Remote") || name.startsWith("Remote ")) {
            return DryRunLibraryType.REMOTE;
        } else if (source != null) {
            final String path = source.getPath();
            if (path.endsWith(".jar") || path.endsWith(".java") || path.endsWith(".class")) {
                return DryRunLibraryType.JAVA;
            } else if (path.endsWith(".py")) {
                return DryRunLibraryType.PYTHON;
            }
        }
        return DryRunLibraryType.UNKNOWN;
    }

    @VisibleForTesting
    RobotDryRunLibraryImport(final String name, final URI source, final DryRunLibraryType type,
            final Set<URI> importers, final List<String> args, final DryRunLibraryImportStatus status,
            final String additionalInfo) {
        this.name = name;
        this.source = source;
        this.type = type;
        this.importers = importers;
        this.args = args;
        this.status = status;
        this.additionalInfo = additionalInfo;
    }

    public String getName() {
        return name;
    }

    public URI getSource() {
        return source;
    }

    public Set<URI> getImporters() {
        return importers;
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

    public void setImporters(final Set<URI> importers) {
        this.importers.clear();
        this.importers.addAll(importers);
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
            return Objects.equals(name, other.name) && Objects.equals(source, other.source)
                    && Objects.equals(type, other.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, source, type);
    }

    @Override
    public String toString() {
        return "RobotDryRunLibraryImport [name=" + name + ", source=" + source + ", type=" + type + ", importers="
                + importers + ", args=" + args + ", status=" + status + ", additionalInfo=" + additionalInfo + "]";
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
