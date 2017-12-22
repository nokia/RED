/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.eclipse.core.resources.IResource;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.LibrariesAutoDiscoverHandler.E4LibrariesAutoDiscoverHandler;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.CombinedLibrariesAutoDiscoverer;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class LibrariesAutoDiscoverHandler extends DIParameterizedHandler<E4LibrariesAutoDiscoverHandler> {

    public LibrariesAutoDiscoverHandler() {
        super(E4LibrariesAutoDiscoverHandler.class);
    }

    public static class E4LibrariesAutoDiscoverHandler {

        @Execute
        public void discoverLibs(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IResource> selectedResources = Selections.getAdaptableElements(selection, IResource.class);

            // TODO: run grouping in job, see RED-1013
            final Map<RobotProject, Collection<RobotSuiteFile>> filesGroupedByProject = RobotSuiteFileCollector
                    .collectGroupedByProject(selectedResources);
            // TODO: for now we want to start autodiscovering only for one project, see RED-1004
            filesGroupedByProject.entrySet().stream().findFirst().ifPresent(
                    entry -> new CombinedLibrariesAutoDiscoverer(entry.getKey(), entry.getValue()).start());
        }
    }
}
