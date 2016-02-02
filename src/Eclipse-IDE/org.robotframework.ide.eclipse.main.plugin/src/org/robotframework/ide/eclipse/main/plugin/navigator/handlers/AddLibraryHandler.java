/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.AddLibraryHandler.E4AddLibraryHandler;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryImporter;
import org.robotframework.red.viewers.Selections;


public class AddLibraryHandler extends DIHandler<E4AddLibraryHandler> {

    public AddLibraryHandler() {
        super(E4AddLibraryHandler.class);
    }

    public static class E4AddLibraryHandler {

        @Inject
        private IEventBroker eventBroker;

        @Execute
        public Object addLibs(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IFile> selectedFiles = Selections.getElements(selection, IFile.class);

            final ReferencedLibraryImporter importer = new ReferencedLibraryImporter();

            for (IFile file : selectedFiles) {
                final RobotProject robotProject = RedPlugin.getModelManager().createProject(file.getProject());
                RobotProjectConfig config = robotProject.getOpenedProjectConfig();
                final boolean inEditor = config != null;
                if (config == null) {
                    config = new RobotProjectConfigReader().readConfiguration(robotProject.getConfigurationFile());
                }

                final Shell shell = Display.getCurrent().getActiveShell();
                final ReferencedLibrary newLibrary = importer.importPythonLib(shell,
                        robotProject.getRuntimeEnvironment(), file.getLocation().toString());

                final List<ReferencedLibrary> addedLibs = new ArrayList<>();
                if (newLibrary != null && config.addReferencedLibrary(newLibrary)) {
                    addedLibs.add(newLibrary);
                }

                if (!addedLibs.isEmpty()) {
                    final RedProjectConfigEventData<List<ReferencedLibrary>> eventData = new RedProjectConfigEventData<>(
                            robotProject.getConfigurationFile(), addedLibs);
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED, eventData);

                    if (!inEditor) {
                        new RobotProjectConfigWriter().writeConfiguration(config, robotProject);
                    }
                }
            }

            return null;
        }
    }
}
