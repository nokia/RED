/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Named;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.LibrariesAutoDiscoverHandler.E4LibrariesAutoDiscoverHandler;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.CombinedLibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer.DiscovererFactory;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscovererWindow;
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
            final Consumer<Collection<RobotDryRunLibraryImport>> summaryHandler = LibrariesAutoDiscovererWindow
                    .openSummary();
            final DiscovererFactory discovererFactory = (robotProject,
                    suites) -> new CombinedLibrariesAutoDiscoverer(robotProject, suites, summaryHandler);
            final WorkspaceJob suiteCollectingJob = RobotSuiteFileCollector.createCollectingJob(selectedResources,
                    suites -> LibrariesAutoDiscoverer.start(suites, discovererFactory));
            suiteCollectingJob.schedule();
        }
    }
}
