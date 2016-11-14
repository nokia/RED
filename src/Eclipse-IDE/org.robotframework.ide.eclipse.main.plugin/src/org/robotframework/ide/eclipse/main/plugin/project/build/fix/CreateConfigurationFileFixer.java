/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;


public class CreateConfigurationFileFixer implements IMarkerResolution {

    @Override
    public String getLabel() {
        return "Create configuration file";
    }

    @Override
    public void run(final IMarker marker) {
        final IProject project = marker.getResource().getProject();
        try {
            new RedEclipseProjectConfigWriter().writeConfiguration(RobotProjectConfig.create(), project);
            marker.getResource().deleteMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_INFINITE);
        } catch (final CoreException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, e.getMessage()),
                    StatusManager.SHOW);
        }
    }
}
