/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.LibrariesAutoDiscoverHandler.E4LibrariesAutoDiscoverHandler;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class LibrariesAutoDiscoverHandler extends DIParameterizedHandler<E4LibrariesAutoDiscoverHandler> {

    public LibrariesAutoDiscoverHandler() {
        super(E4LibrariesAutoDiscoverHandler.class);
    }

    public static class E4LibrariesAutoDiscoverHandler {

        @Execute
        public void addLibs(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IResource> selectedResources = Selections.getAdaptableElements(selection, IResource.class);

            final Set<IFile> files = new HashSet<>();
            // we just want to autodiscover for only one project
            final IProject suitesProject = selectedResources.isEmpty() ? null : selectedResources.get(0).getProject();

            for (final IResource resource : selectedResources) {
                if (resource.getProject().equals(suitesProject)) {
                    if (resource.getType() == IResource.PROJECT) {
                        startAutoDiscovering((IProject) resource, new ArrayList<>());
                        return;
                    } else if (resource.getType() == IResource.FILE) {
                        files.add((IFile) resource);
                    } else if (resource.getType() == IResource.FOLDER) {
                        addFilesFromFolder((IFolder) resource, files);
                    }
                }
            }

            if (!files.isEmpty()) {
                startAutoDiscovering(suitesProject, new ArrayList<>(files));
            }
        }

        private void startAutoDiscovering(final IProject project, final List<? extends IResource> resources) {
            final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
            new LibrariesAutoDiscoverer(robotProject, resources).start();
        }

        private void addFilesFromFolder(final IFolder folder, final Set<IFile> files) {
            try {
                for (final IResource resource : folder.members()) {
                    if (resource.getType() == IResource.FILE) {
                        files.add((IFile) resource);
                    } else if (resource.getType() == IResource.FOLDER) {
                        addFilesFromFolder((IFolder) resource, files);
                    }
                }
            } catch (final CoreException e) {
                // nothing to autodiscover
            }
        }
    }
}
