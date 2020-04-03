/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

public class LibraryLocationFinder {

    public static Optional<IPath> findPath(final RobotProject robotProject, final LibrarySpecification libSpec) {
        final Optional<File> source = libSpec.getSource();
        if (source.isPresent()) {
            return source.map(LibraryLocationFinder::resolvePathFromSpec);

        } else if (isLibraryFrom(libSpec, robotProject.getStandardLibraries())) {
            return findStandardLibPath(robotProject, libSpec);

        } else if (isLibraryFrom(libSpec, robotProject.getReferencedLibraries())) {
            return findReferenceLibPath(libSpec.getDescriptor());
        }
        return Optional.empty();
    }

    public static Optional<KeywordLocation> findKeywordDefinition(
            final LibrarySpecification libSpec,
            final KeywordSpecification kwSpec) {

        final Optional<File> kwSource = kwSpec.getSource();
        final Optional<File> libSource = libSpec.getSource();
        
        final File source;
        if (kwSource.isPresent()) {
            source = kwSource.get();
        } else if (libSource.isPresent()) {
            source = libSource.get();
        } else {
            source = null;
        }

        final Integer lineNumber = kwSpec.getLineNumber();
        return lineNumber == null ? Optional.empty()
                : Optional.ofNullable(source)
                        .map(LibraryLocationFinder::resolvePathFromSpec)
                        .map(path -> new KeywordLocation(path, lineNumber));
    }

    private static IPath resolvePathFromSpec(final File sourceInSpec) {
        // From RF 3.2 library specification files contain sources which are always absolute
        // (we change all relative paths into absolute in red_libraries.py module

        return Path.fromOSString(sourceInSpec.getAbsolutePath());
    }

    private static boolean isLibraryFrom(final LibrarySpecification spec,
            final Map<LibraryDescriptor, LibrarySpecification> libs) {
        return libs.keySet().stream().anyMatch(descriptor -> descriptor.equals(spec.getDescriptor()));
    }

    private static Optional<IPath> findStandardLibPath(final RobotProject robotProject,
            final LibrarySpecification libSpec) {
        final IRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
        final Optional<File> standardLibraryPath = runtimeEnvironment.getStandardLibraryPath(libSpec.getName());
        return standardLibraryPath.map(file -> new Path(file.getAbsolutePath()));
    }

    private static Optional<IPath> findReferenceLibPath(final LibraryDescriptor descriptor) {
        return Optional
                .of(RedWorkspace.Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(descriptor.getPath())));
    }

    static class KeywordLocation {

        private final IPath sourcePath;

        private final int line;

        private KeywordLocation(final IPath sourcePath, final int line) {
            this.sourcePath = sourcePath;
            this.line = line;
        }

        IPath getSourcePath() {
            return sourcePath;
        }

        int getLine() {
            return line;
        }
    }
}
