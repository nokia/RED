/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

/**
 * @author bembenek
 */
public class LibrariesConfigUpdater {

    protected final RobotProject robotProject;

    protected final RobotProjectConfig config;

    private final boolean isEditorOpened;

    private final List<ReferencedLibrary> addedLibraries;

    public LibrariesConfigUpdater(final RobotProject robotProject) {
        this.robotProject = robotProject;
        RobotProjectConfig config = robotProject.getOpenedProjectConfig();
        this.isEditorOpened = config == null;
        if (config == null) {
            config = new RedEclipseProjectConfigReader().readConfiguration(robotProject.getConfigurationFile());
        }
        this.config = config;
        this.addedLibraries = new ArrayList<>();
    }

    public RobotProjectConfig getConfig() {
        return config;
    }

    public void addLibraries(final Collection<ReferencedLibrary> libraries) {
        for (final ReferencedLibrary library : libraries) {
            if (config.addReferencedLibrary(library)) {
                addedLibraries.add(library);
            }
        }
    }

    public void finalizeLibrariesAdding(final IEventBroker eventBroker) {
        RedPlugin.logInfo("ADDED LIBRARIES: \n" + addedLibraries.toString());
        if (!addedLibraries.isEmpty()) {
            robotProject.clearConfiguration();
            robotProject.clearKwSources();
            if (isEditorOpened) {
                new RedEclipseProjectConfigWriter().writeConfiguration(config, robotProject);
            }
            fireEvents(eventBroker);
            addedLibraries.clear();
        }
    }

    private void fireEvents(final IEventBroker eventBroker) {
        final RedProjectConfigEventData<List<ReferencedLibrary>> eventData = new RedProjectConfigEventData<>(
                robotProject.getConfigurationFile(), addedLibraries);
        eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED, eventData);
    }

}
