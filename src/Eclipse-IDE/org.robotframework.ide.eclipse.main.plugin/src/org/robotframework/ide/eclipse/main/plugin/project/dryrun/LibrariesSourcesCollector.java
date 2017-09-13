/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

/**
 * @author mmarzec
 */
class LibrariesSourcesCollector {

    private final RobotProject robotProject;

    private final Set<String> pythonpathLocations = new LinkedHashSet<>();

    private final Set<String> classpathLocations = new LinkedHashSet<>();

    LibrariesSourcesCollector(final RobotProject robotProject) {
        this.robotProject = robotProject;
    }

    void collectPythonAndJavaLibrariesSources() throws CoreException {
        collectPathLocations(robotProject.getProject());
    }

    void collectPythonAndJavaLibrariesSources(final int maxDepth) throws CoreException {
        collectPathLocations(robotProject.getProject(), 0, maxDepth);
    }

    private void collectPathLocations(final IContainer parent) throws CoreException {
        for (final IResource resource : parent.members()) {
            if (resource.getType() == IResource.FILE) {
                checkFileExtensionAndAddToProperLocations((IFile) resource);
            } else if (resource.getType() == IResource.FOLDER) {
                collectPathLocations((IFolder) resource);
            }
        }
    }

    private void collectPathLocations(final IContainer parent, final int currentDepth, final int maxDepth)
            throws CoreException {
        for (final IResource resource : parent.members()) {
            if (resource.getType() == IResource.FILE) {
                checkFileExtensionAndAddToProperLocations((IFile) resource);
            } else if (resource.getType() == IResource.FOLDER && currentDepth < maxDepth) {
                collectPathLocations((IFolder) resource, currentDepth + 1, maxDepth);
            }
        }
    }

    private void checkFileExtensionAndAddToProperLocations(final IFile file) {
        final String fileExtension = file.getFileExtension();
        final IPath fileLocation = file.getLocation();
        if (fileExtension != null && fileLocation != null) {
            if (fileExtension.equals("py")) {
                pythonpathLocations.add(fileLocation.toFile().getParent());
            } else if (fileExtension.equals("jar")) {
                classpathLocations.add(fileLocation.toOSString());
            }
        }
    }

    EnvironmentSearchPaths getEnvironmentSearchPaths() {
        final EnvironmentSearchPaths environmentSearchPaths = new EnvironmentSearchPaths(robotProject.getClasspath(),
                robotProject.getPythonpath());
        pythonpathLocations.forEach(environmentSearchPaths::addPythonPath);
        classpathLocations.forEach(environmentSearchPaths::addClassPath);
        return environmentSearchPaths;
    }

}
