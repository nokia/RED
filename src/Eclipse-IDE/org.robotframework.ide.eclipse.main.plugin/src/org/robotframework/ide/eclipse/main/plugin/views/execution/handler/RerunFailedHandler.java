/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import java.io.File;
import java.net.URI;
import java.util.Optional;

import javax.inject.Named;

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
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewWrapper;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.RerunFailedHandler.E4ShowFailedOnlyHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class RerunFailedHandler extends DIParameterizedHandler<E4ShowFailedOnlyHandler> {

    public RerunFailedHandler() {
        super(E4ShowFailedOnlyHandler.class);
    }

    public static class E4ShowFailedOnlyHandler {

        @Execute
        public void toggleShowFailedOnly(@Named(ISources.ACTIVE_PART_NAME) final ExecutionViewWrapper view) {
            @SuppressWarnings("restriction")
            final ExecutionView executionView = view.getComponent();
            final Optional<RobotTestsLaunch> launch = executionView.getCurrentlyShownLaunch();

            if (launch.isPresent()) {
                final WorkspaceJob job = new WorkspaceJob("Launching Robot Tests") {

                    @Override
                    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                        final ILaunchConfiguration launchConfig = launch.get().getLaunchConfiguration();
                        if (launchConfig != null && launchConfig.exists()) {
                            // FIXME : do not use -R launching; instead collect failed tests from
                            // the tree and launch test basing on -s and -t switches
                            final Optional<URI> outputFile = launch.get()
                                    .getExecutionData(ExecutionStatusStore.class)
                                    .map(ExecutionStatusStore::getOutputFilePath);
                            if (outputFile.isPresent()) {
                                final ILaunchConfigurationWorkingCopy launchConfigCopy = launchConfig
                                        .copy(launchConfig.getName());
                                RobotLaunchConfiguration.fillForFailedTestsRerun(launchConfigCopy,
                                        new File(outputFile.get()).getAbsolutePath());
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
    }
}
