/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionStatusStore;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewWrapper;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.RerunFailedHandler.E4RerunFailedHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.annotations.VisibleForTesting;

public class RerunFailedHandler extends DIParameterizedHandler<E4RerunFailedHandler> {

    public RerunFailedHandler() {
        super(E4RerunFailedHandler.class);
    }

    public static class E4RerunFailedHandler {

        @Execute
        public void rerunFailed(@Named(ISources.ACTIVE_PART_NAME) final ExecutionViewWrapper view) {
            view.getComponent().getCurrentlyShownLaunch().ifPresent(launch -> {
                final WorkspaceJob job = new WorkspaceJob("Launching Robot Tests") {

                    @Override
                    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                        final ILaunchConfigurationWorkingCopy launchConfigCopy = getConfig(launch);
                        launchConfigCopy.launch(ILaunchManager.RUN_MODE, monitor);
                        return Status.OK_STATUS;
                    }
                };
                job.setUser(false);
                job.schedule();
            });
        }

        @VisibleForTesting
        static ILaunchConfigurationWorkingCopy getConfig(final RobotTestsLaunch launch) throws CoreException {
            final ILaunchConfiguration launchConfig = launch.getLaunchConfiguration();

            if (launchConfig != null && launchConfig.exists()) {
                final ILaunchConfigurationWorkingCopy launchConfigCopy = launchConfig.copy(launchConfig.getName());
                final IProject project = new RobotLaunchConfiguration(launchConfigCopy).getProject();
                final ExecutionStatusStore statusStore = launch.getExecutionData(ExecutionStatusStore.class).get();
                final Map<String, List<String>> failedSuitesPaths = statusStore.getFailedSuitePaths(project);

                if (!failedSuitesPaths.isEmpty()) {
                    RobotLaunchConfiguration.fillForFailedOrNonExecutedTestsRerun(launchConfigCopy, failedSuitesPaths);
                    return launchConfigCopy;
                } else {
                    throw newCoreException("Failed tests do not exist");
                }
            } else {
                throw newCoreException("Launch configuration does not exist");
            }
        }
    }
}
