/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.mockito.ArgumentMatcher;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;

import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;

class LibraryImports {

    static RobotDryRunLibraryImport createImport(final DryRunLibraryImportStatus status, final String name) {
        final RobotDryRunLibraryImport libImport = new RobotDryRunLibraryImport(name);
        libImport.setStatus(status);
        return libImport;
    }

    static RobotDryRunLibraryImport createImport(final DryRunLibraryImportStatus status, final String name,
            final IFile source) {
        final URI sourcePath = source.getLocation().toFile().toURI();
        final RobotDryRunLibraryImport libImport = new RobotDryRunLibraryImport(name, sourcePath);
        libImport.setStatus(status);
        return libImport;
    }

    static RobotDryRunLibraryImport createImport(final DryRunLibraryImportStatus status, final String name,
            final Set<IFile> importers) {
        final RobotDryRunLibraryImport libImport = createImport(status, name);
        libImport.setImportersPaths(
                importers.stream().map(importer -> importer.getLocation().toFile().toURI()).collect(toSet()));
        return libImport;
    }

    static RobotDryRunLibraryImport createImport(final DryRunLibraryImportStatus status, final String name,
            final IFile source, final Set<IFile> importers) {
        final RobotDryRunLibraryImport libImport = createImport(status, name, source);
        libImport.setImportersPaths(
                importers.stream().map(importer -> importer.getLocation().toFile().toURI()).collect(toSet()));
        return libImport;
    }

    static ArgumentMatcher<Collection<RobotDryRunLibraryImport>> hasLibImports(
            final RobotDryRunLibraryImport... expectedLibImports) {
        return actualLibImports -> {
            final RobotDryRunLibraryImportEquivalence eq = new RobotDryRunLibraryImportEquivalence();
            final Set<Wrapper<RobotDryRunLibraryImport>> actual = actualLibImports.stream().map(eq::wrap).collect(
                    toSet());
            final Set<Wrapper<RobotDryRunLibraryImport>> expected = Stream.of(expectedLibImports).map(eq::wrap).collect(
                    toSet());
            return actualLibImports.size() == expectedLibImports.length && actual.equals(expected);
        };
    }

    private static class RobotDryRunLibraryImportEquivalence extends Equivalence<RobotDryRunLibraryImport> {

        @Override
        protected boolean doEquivalent(final RobotDryRunLibraryImport libImport1,
                final RobotDryRunLibraryImport libImport2) {
            return Objects.equals(libImport1.getStatus(), libImport2.getStatus())
                    && Objects.equals(libImport1.getName(), libImport2.getName())
                    && Objects.equals(libImport1.getSourcePath(), libImport2.getSourcePath())
                    && Objects.equals(libImport1.getType(), libImport2.getType())
                    && Objects.equals(libImport1.getImportersPaths(), libImport2.getImportersPaths());
        }

        @Override
        protected int doHash(final RobotDryRunLibraryImport libImport) {
            return Objects.hash(libImport.getStatus(), libImport.getName(), libImport.getSourcePath(),
                    libImport.getType(), libImport.getImportersPaths());
        }
    }
}
