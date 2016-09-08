/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.execution;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfiguration;

public class RerunFailedOnlyAction extends Action implements IWorkbenchAction {

    private static final String ID = "org.robotframework.action.executionView.RerunFailedOnlyAction";

    private String outputFilePath;

    public RerunFailedOnlyAction() {
        super("Rerun Failed Tests Only", RedImages.getRelaunchFailedImage());
        setId(ID);
    }

    @Override
    public void run() {

        final ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
        if (launches != null && launches.length > 0) {
            final WorkspaceJob job = new WorkspaceJob("Launching Robot Tests") {

                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                    if (outputFilePath != null) {
                        final ILaunchConfiguration launchConfig = launches[0].getLaunchConfiguration();
                        if (launchConfig != null) {
                            final ILaunchConfigurationWorkingCopy launchConfigCopy = launchConfig.copy(launchConfig.getName());
                            RobotLaunchConfiguration.prepareRerunFailedTestsConfiguration(launchConfigCopy, outputFilePath);
                            if (launchConfigCopy != null) {
                                launchConfigCopy.launch(ILaunchManager.RUN_MODE, monitor);
                            }
                        }
                    }
                    return Status.OK_STATUS;
                }
            };
            job.setUser(false);
            job.schedule();
        }
    }

    @Override
    public void dispose() {
    }

    public void setOutputFilePath(final String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }
}
