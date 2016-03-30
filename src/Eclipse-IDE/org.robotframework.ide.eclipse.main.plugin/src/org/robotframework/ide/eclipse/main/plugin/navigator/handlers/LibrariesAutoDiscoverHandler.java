/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.LibrariesAutoDiscoverHandler.E4LibrariesAutoDiscoverHandler;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesAutoDiscoverer;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class LibrariesAutoDiscoverHandler extends DIParameterizedHandler<E4LibrariesAutoDiscoverHandler> {

    public LibrariesAutoDiscoverHandler() {
        super(E4LibrariesAutoDiscoverHandler.class);
    }

    public static class E4LibrariesAutoDiscoverHandler {

        @Inject
        private IEventBroker eventBroker;

        @Execute
        public Object addLibs(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IResource> selectedResources = Selections.getElements(selection, IResource.class);

            final List<IResource> suitesList = new ArrayList<>();
            IProject suitesProject = null;

            for (final IResource resource : selectedResources) {
                if (resource.getType() == IResource.PROJECT) {
                    final IProject project = (IProject) resource;
                    final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
                    new LibrariesAutoDiscoverer(robotProject, Collections.<IResource> emptyList(), eventBroker).start();
                    return null;

                } else if (resource.getType() == IResource.FILE || resource.getType() == IResource.FOLDER) {
                    if (suitesProject == null) {
                        suitesProject = resource.getProject();
                    }
                    if (resource.getProject().equals(suitesProject)) {
                        suitesList.add(resource);
                    }
                }
            }

            if (!suitesList.isEmpty()) {
                final RobotProject robotProject = RedPlugin.getModelManager().createProject(suitesProject);
                new LibrariesAutoDiscoverer(robotProject, suitesList, eventBroker).start();
            }

            return null;
        }
    }
}
