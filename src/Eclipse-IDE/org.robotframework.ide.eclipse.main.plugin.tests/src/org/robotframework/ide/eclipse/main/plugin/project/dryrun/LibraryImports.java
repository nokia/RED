/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.eclipse.core.resources.IFile;
import org.mockito.ArgumentMatcher;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;

import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;

class LibraryImports {

    static RobotDryRunLibraryImport createImport(final DryRunLibraryImportStatus status, final String name) {
        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createUnknown(name);
        libImport.setStatus(status);
        return libImport;
    }

    static RobotDryRunLibraryImport createImport(final DryRunLibraryImportStatus status, final String name,
            final URI sourcePath, final Set<IFile> importers) {
        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown(name, sourcePath);
        libImport.setStatus(status);
        libImport.setImporters(importers.stream().map(IFile::getLocationURI).collect(toSet()));
        return libImport;
    }

    static RobotDryRunLibraryImport createImport(final DryRunLibraryImportStatus status, final String name,
            final IFile source) {
        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown(name, source.getLocationURI());
        libImport.setStatus(status);
        return libImport;
    }

    static RobotDryRunLibraryImport createImport(final DryRunLibraryImportStatus status, final String name,
            final Set<IFile> importers) {
        final RobotDryRunLibraryImport libImport = createImport(status, name);
        libImport.setImporters(importers.stream().map(IFile::getLocationURI).collect(toSet()));
        return libImport;
    }

    static RobotDryRunLibraryImport createImport(final DryRunLibraryImportStatus status, final String name,
            final IFile source, final Set<IFile> importers) {
        final RobotDryRunLibraryImport libImport = createImport(status, name, source);
        libImport.setImporters(importers.stream().map(IFile::getLocationURI).collect(toSet()));
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
            return actualLibImports.size() == expectedLibImports.length && actual.equals(expected)
                    && onlyNotAddedImportsHaveAdditionalInfo(actualLibImports);
        };
    }

    static Condition<? super Iterable<? extends RobotDryRunLibraryImport>> onlyLibImports(
            final RobotDryRunLibraryImport... expectedLibImports) {
        return new Condition<Iterable<? extends RobotDryRunLibraryImport>>() {

            @Override
            public boolean matches(final Iterable<? extends RobotDryRunLibraryImport> actualLibImports) {
                return hasLibImports(expectedLibImports).matches(newArrayList(actualLibImports));
            }
        };
    }

    private static boolean onlyNotAddedImportsHaveAdditionalInfo(
            final Collection<RobotDryRunLibraryImport> libImports) {
        final boolean allNotAddedHaveNotEmptyAdditionalInfo = libImports.stream()
                .filter(libImport -> libImport.getStatus() == DryRunLibraryImportStatus.NOT_ADDED)
                .noneMatch(libImport -> libImport.getAdditionalInfo().isEmpty());
        final boolean allAddedOrExistingHaveEmptyAdditionalInfo = libImports.stream()
                .filter(libImport -> libImport.getStatus() != DryRunLibraryImportStatus.NOT_ADDED)
                .allMatch(libImport -> libImport.getAdditionalInfo().isEmpty());
        return allNotAddedHaveNotEmptyAdditionalInfo && allAddedOrExistingHaveEmptyAdditionalInfo;
    }

    private static class RobotDryRunLibraryImportEquivalence extends Equivalence<RobotDryRunLibraryImport> {

        @Override
        protected boolean doEquivalent(final RobotDryRunLibraryImport libImport1,
                final RobotDryRunLibraryImport libImport2) {
            return Objects.equals(libImport1.getStatus(), libImport2.getStatus())
                    && Objects.equals(libImport1.getName(), libImport2.getName())
                    && Objects.equals(libImport1.getSource(), libImport2.getSource())
                    && Objects.equals(libImport1.getType(), libImport2.getType())
                    && Objects.equals(libImport1.getImporters(), libImport2.getImporters());
        }

        @Override
        protected int doHash(final RobotDryRunLibraryImport libImport) {
            return Objects.hash(libImport.getStatus(), libImport.getName(), libImport.getSource(), libImport.getType(),
                    libImport.getImporters());
        }
    }
}
