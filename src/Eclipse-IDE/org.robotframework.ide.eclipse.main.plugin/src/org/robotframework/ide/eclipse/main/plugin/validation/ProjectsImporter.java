/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.validation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;

import com.google.common.io.Files;

/**
 * @author Michal Anglart
 *
 */
class ProjectsImporter {

    private final BuildLogger logger;

    ProjectsImporter(final BuildLogger logger) {
        this.logger = logger;
    }

    void importNeededProjects(final List<String> projectsToImport)
            throws InvocationTargetException, InterruptedException, CoreException, IOException {

        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IPath wsLocation = workspace.getRoot().getLocation();

        for (final String path : projectsToImport) {
            logger.log("Importing project: " + path);

            final IPath originalLocation = new Path(path);
            final IPath wsProjectLocation = wsLocation.append(originalLocation.lastSegment());

            final File from = new File(originalLocation.toFile().toURI());
            final File to = wsProjectLocation.toFile();
            if (from.equals(to)) {
                logger.log("WARNING: project " + path + " is already in the workspace");
            } else {
                copyProjectFiles(from, to);
            }

            final IProjectDescription description = workspace
                    .loadProjectDescription(wsProjectLocation.append(".project"));
            final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
            if (!project.exists()) {
                project.create(description, null);
            }
            project.open(null);

            logger.log("Project: " + path + " was succesfully imported");
        }
    }

    private void copyProjectFiles(final File sourceLocation, final File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            final String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyProjectFiles(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            Files.copy(sourceLocation, targetLocation);
        }
    }
}
