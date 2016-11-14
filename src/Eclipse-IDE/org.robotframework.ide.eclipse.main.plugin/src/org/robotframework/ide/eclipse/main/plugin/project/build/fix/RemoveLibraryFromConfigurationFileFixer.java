/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ConfigFileProblem;


public class RemoveLibraryFromConfigurationFileFixer implements IMarkerResolution {

    @Override
    public String getLabel() {
        return "Remove library";
    }

    @Override
    public void run(final IMarker marker) {
        final IResource redFile = marker.getResource();
        if (redFile == null || !redFile.exists() || !redFile.getName().equals(RobotProjectConfig.FILENAME)) {
            throw new IllegalStateException("Marker is assigned to invalid file");
        }
        final int indexOfLibrary = marker.getAttribute(ConfigFileProblem.LIBRARY_INDEX, -1);
        if (indexOfLibrary < 0) {
            return;
        }
        
        final RobotProjectConfig config = new RedEclipseProjectConfigReader().readConfiguration(redFile.getProject());
        if (indexOfLibrary < config.getLibraries().size()) {
            config.getLibraries().remove(indexOfLibrary);
        }
        new RedEclipseProjectConfigWriter().writeConfiguration(config, redFile.getProject());
        try {
            marker.delete();
        } catch (final CoreException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, e.getMessage()),
                    StatusManager.SHOW);
        }
    }
}
