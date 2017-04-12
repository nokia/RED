/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

/**
 * @author mmarzec
 */
public class LibrariesSourcesCollector {

    private final RobotProject robotProject;

    private final Set<String> pythonpathLocations = new HashSet<>();

    private final Set<String> classpathLocations = new HashSet<>();

    public LibrariesSourcesCollector(final RobotProject robotProject) {
        this.robotProject = robotProject;
    }

    public void collectPythonAndJavaLibrariesSources() throws CoreException {
        if (shouldCollectLibrariesRecursively()) {
            collectLocationsWithPythonAndJavaMembersRecursively(robotProject.getProject().members());
        } else {
            collectOnlyParentLocationsWithPythonAndJavaMembers(robotProject.getProject().members());
        }

        final IPath projectLocation = robotProject.getProject().getLocation();
        if (projectLocation != null) {
            pythonpathLocations.add(projectLocation.toOSString());
        }
        pythonpathLocations.addAll(robotProject.getPythonpath());

        classpathLocations.addAll(robotProject.getClasspath());
    }

    private boolean shouldCollectLibrariesRecursively() throws CoreException {
        final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
        if (runtimeEnvironment == null) {
            throw newCoreException(
                    "There is no active runtime environment for project '" + robotProject.getName() + "'");
        }
        return !runtimeEnvironment.isVirtualenv()
                || RedPlugin.getDefault().getPreferences().isProjectModulesRecursiveAdditionOnVirtualenvEnabled();
    }

    private void collectLocationsWithPythonAndJavaMembersRecursively(final IResource[] members) throws CoreException {
        if (members != null) {
            for (int i = 0; i < members.length; i++) {
                final IResource resource = members[i];
                if (resource.getType() == IResource.FILE) {
                    checkFileExtensionAndAddToProperLocations(resource);
                } else if (resource.getType() == IResource.FOLDER) {
                    collectLocationsWithPythonAndJavaMembersRecursively(((IFolder) resource).members());
                }
            }
        }
    }

    private void collectOnlyParentLocationsWithPythonAndJavaMembers(final IResource[] members) throws CoreException {
        if (members != null) {
            for (int i = 0; i < members.length; i++) {
                final IResource resource = members[i];
                if (resource.getType() == IResource.FOLDER) {
                    final IResource[] folderMembers = ((IFolder) resource).members();
                    for (int j = 0; j < folderMembers.length; j++) {
                        final IResource folderMember = folderMembers[j];
                        if (folderMember.getType() == IResource.FILE) {
                            checkFileExtensionAndAddToProperLocations(folderMember);
                        }
                    }
                }
            }
        }
    }

    public void checkFileExtensionAndAddToProperLocations(final IResource resource) {
        final String fileExtension = resource.getFileExtension();
        final IPath fileLocation = resource.getLocation();
        if (fileExtension != null && fileLocation != null) {
            if (fileExtension.equals("py")) {
                pythonpathLocations.add(fileLocation.toFile().getParent());
            } else if (fileExtension.equals("jar")) {
                classpathLocations.add(fileLocation.toOSString());
            }
        }
    }

    public EnvironmentSearchPaths getEnvironmentSearchPaths() {
        return new EnvironmentSearchPaths(classpathLocations, pythonpathLocations);
    }

}
