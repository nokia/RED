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
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

public class LibraryLocationFinder {

    public static Optional<IPath> findPath(final RobotProject robotProject, final LibrarySpecification libSpec) {
        if (isLibraryFrom(libSpec, robotProject.getStandardLibraries())) {
            return findStandardLibPath(robotProject, libSpec);
        } else if (isLibraryFrom(libSpec, robotProject.getReferencedLibraries())) {
            final LibraryDescriptor descriptor = libSpec.getDescriptor();
            return Optional
                    .of(RedWorkspace.Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(descriptor.getPath())));
        }
        return Optional.empty();
    }

    public static Optional<IPath> findFullPath(final RobotProject robotProject, final LibrarySpecification libSpec) {
        if (isLibraryFrom(libSpec, robotProject.getStandardLibraries())) {
            return findStandardLibPath(robotProject, libSpec);
        } else if (isLibraryFrom(libSpec, robotProject.getReferencedLibraries())) {
            final LibraryDescriptor descriptor = libSpec.getDescriptor();
            return findReferenceLibPath(descriptor, libSpec);
        }
        return Optional.empty();
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

    private static Optional<IPath> findReferenceLibPath(final LibraryDescriptor descriptor,
            final LibrarySpecification libSpec) {
        final IPath libPath = RedWorkspace.Paths
                .toAbsoluteFromWorkspaceRelativeIfPossible(new Path(descriptor.getPath()));
        return Optional.of(libPath).filter(path -> path.toFile().exists());
    }
}
