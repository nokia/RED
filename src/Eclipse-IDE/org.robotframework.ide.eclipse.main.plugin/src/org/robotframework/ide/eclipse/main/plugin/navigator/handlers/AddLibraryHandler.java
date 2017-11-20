/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.AddLibraryHandler.E4AddLibraryHandler;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesConfigUpdater;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryImporter;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;


public class AddLibraryHandler extends DIParameterizedHandler<E4AddLibraryHandler> {

    public AddLibraryHandler() {
        super(E4AddLibraryHandler.class);
    }

    public static class E4AddLibraryHandler {

        @Inject
        private IEventBroker eventBroker;

        @Execute
        public void addLibs(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IFile> selectedFiles = Selections.getAdaptableElements(selection, IFile.class);

            final ReferencedLibraryImporter importer = new ReferencedLibraryImporter(
                    Display.getCurrent().getActiveShell());

            for (final IFile file : selectedFiles) {
                final RobotProject robotProject = RedPlugin.getModelManager().createProject(file.getProject());
                final LibrariesConfigUpdater updater = new LibrariesConfigUpdater(robotProject);

                final Collection<ReferencedLibrary> newLibraries = importer.importPythonLib(
                        robotProject.getRuntimeEnvironment(), robotProject.getProject(), updater.getConfig(),
                        file.getLocation().toString());

                updater.addLibraries(newLibraries);
                updater.finalizeLibrariesAdding(eventBroker);
            }
        }
    }
}
