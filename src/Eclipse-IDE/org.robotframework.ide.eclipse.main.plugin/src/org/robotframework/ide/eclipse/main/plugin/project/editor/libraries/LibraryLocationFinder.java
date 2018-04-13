/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.io.File;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

public class LibraryLocationFinder {

    public static Optional<IPath> findPath(final RobotModel model, final IProject project,
            final LibrarySpecification libSpec) {
        final RobotProject robotProject = model.createRobotProject(project);
        if (robotProject.isStandardLibrary(libSpec)) {
            return findStandardLibPath(robotProject, libSpec);
        } else if (robotProject.isReferencedLibrary(libSpec)) {
            final LibraryDescriptor descriptor = libSpec.getDescriptor();
            return findReferenceLibPath(descriptor, libSpec);
        }
        return Optional.empty();
    }

    private static Optional<IPath> findStandardLibPath(final RobotProject robotProject,
            final LibrarySpecification libSpec) {
        final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
        final File standardLibraryPath = runtimeEnvironment.getStandardLibraryPath(libSpec.getName());
        return Optional.ofNullable(standardLibraryPath).map(path -> new Path(path.getAbsolutePath()));
    }

    private static Optional<IPath> findReferenceLibPath(final LibraryDescriptor descriptor,
            final LibrarySpecification libSpec) {
        final IPath libPath = RedWorkspace.Paths
                .toAbsoluteFromWorkspaceRelativeIfPossible(new Path(descriptor.getFilepath()));
        final Optional<IPath> libPathWithExtension = tryToFindPathWithExtension(libPath);
        if (libPathWithExtension.isPresent()) {
            return libPathWithExtension;
        } else if (libSpec.getName().contains(".")) {
            final Optional<IPath> pathWithoutQualifiedPart = tryToFindPathWithoutQualifiedPart(libPath);
            if (pathWithoutQualifiedPart.isPresent()) {
                return pathWithoutQualifiedPart;
            }
        }
        return tryToFindPathToModule(libPath);
    }

    private static Optional<IPath> tryToFindPathWithExtension(final IPath libPath) {
        final IPath pathWithExtension = libPath.addFileExtension("py");
        return Optional.of(pathWithExtension).filter(path -> path.toFile().exists());
    }

    private static Optional<IPath> tryToFindPathWithoutQualifiedPart(final IPath libPath) {
        final IPath pathWithoutQualifiedPart = libPath.removeLastSegments(1).addFileExtension("py");
        return Optional.of(pathWithoutQualifiedPart).filter(path -> path.toFile().exists());
    }

    private static Optional<IPath> tryToFindPathToModule(final IPath libPath) {
        final IPath pathToModule = libPath.append("__init__.py");
        return Optional.of(pathToModule).filter(path -> path.toFile().exists());
    }
}
