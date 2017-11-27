/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.LibrariesAutoDiscoverHandler.E4LibrariesAutoDiscoverHandler;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.collect.Iterables;

public class LibrariesAutoDiscoverHandler extends DIParameterizedHandler<E4LibrariesAutoDiscoverHandler> {

    public LibrariesAutoDiscoverHandler() {
        super(E4LibrariesAutoDiscoverHandler.class);
    }

    public static class E4LibrariesAutoDiscoverHandler {

        @Execute
        public void discoverLibs(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IResource> selectedResources = Selections.getAdaptableElements(selection, IResource.class);

            final Map<IProject, Collection<RobotSuiteFile>> filesGroupedByProject = RobotSuiteFileCollector
                    .collectGroupedByProject(selectedResources);
            // we just want to autodiscover for only one project
            final Entry<IProject, Collection<RobotSuiteFile>> firstEntry = Iterables
                    .getFirst(filesGroupedByProject.entrySet(), null);
            if (firstEntry != null) {
                final RobotProject robotProject = RedPlugin.getModelManager().createProject(firstEntry.getKey());
                final List<IFile> suites = firstEntry.getValue().stream().map(RobotSuiteFile::getFile).collect(
                        Collectors.toList());
                new LibrariesAutoDiscoverer(robotProject, suites).start();
            }
        }
    }
}
