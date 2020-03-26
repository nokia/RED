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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent.ExecutionMode;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionStatusStore;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewWrapper;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.RerunFailedHandler.E4RerunFailedHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CaseFormat;

public class RerunFailedHandler extends DIParameterizedHandler<E4RerunFailedHandler> implements IElementUpdater {

    public static final String COMMAND_ID = "org.robotframework.red.view.execution.rerunFailedTests";

    public RerunFailedHandler() {
        super(E4RerunFailedHandler.class);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void updateElement(final UIElement element, final Map parameters) {
        final IWorkbenchWindow activeWindow = (IWorkbenchWindow) element.getServiceLocator();
        final ExecutionMode execMode = ExecutionViewPropertyTester.getExecutionMode(activeWindow);

        setTooltip(element, execMode);
    }

    @VisibleForTesting
    void setTooltip(final UIElement element, final ExecutionMode execMode) {
        element.setTooltip("Rerun Failed " + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, execMode.name()));
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
                    RobotLaunchConfiguration.fillForFailedTestsRerun(launchConfigCopy, failedSuitesPaths);
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
