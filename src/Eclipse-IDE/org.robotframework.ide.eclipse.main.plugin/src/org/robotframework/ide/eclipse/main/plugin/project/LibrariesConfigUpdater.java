/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

/**
 * @author bembenek
 */
public class LibrariesConfigUpdater {

    protected final RobotProject robotProject;

    protected final RobotProjectConfig config;

    private final boolean isConfigClosed;

    private final List<Object> addedLibraries;

    public static LibrariesConfigUpdater createFor(final RobotProject project) {
        final Optional<RobotProjectConfig> openedConfig = project.getOpenedProjectConfig();
        final RobotProjectConfig config = openedConfig.orElseGet(project::getRobotProjectConfig);
        return new LibrariesConfigUpdater(project, config, !openedConfig.isPresent());
    }

    protected LibrariesConfigUpdater(final RobotProject project, final RobotProjectConfig config,
            final boolean isConfigClosed) {
        this.robotProject = project;
        this.config = config;
        this.isConfigClosed = isConfigClosed;
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

    public void addRemoteLocation(final RemoteLocation remoteLocation) {
        if (config.addRemoteLocation(remoteLocation)) {
            addedLibraries.add(remoteLocation);
        }
    }

    public void finalizeLibrariesAdding(final IEventBroker eventBroker) {
        if (!addedLibraries.isEmpty()) {
            if (isConfigClosed) {
                robotProject.clearConfiguration();
                robotProject.clearKwSources();
                new RedEclipseProjectConfigWriter().writeConfiguration(config, robotProject);
            }
            fireEvents(eventBroker);
            addedLibraries.clear();
        }
    }

    private void fireEvents(final IEventBroker eventBroker) {
        final RedProjectConfigEventData<List<Object>> eventData = new RedProjectConfigEventData<>(
                robotProject.getConfigurationFile(), addedLibraries);
        eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED, eventData);
    }

}
