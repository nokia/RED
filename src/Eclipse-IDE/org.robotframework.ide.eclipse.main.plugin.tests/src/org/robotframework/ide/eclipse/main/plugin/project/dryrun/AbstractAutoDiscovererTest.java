/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.AbstractAutoDiscoverer.AutoDiscovererException;
import org.robotframework.red.junit.ProjectProvider;

public class AbstractAutoDiscovererTest {

    private static final String PROJECT_NAME = AbstractAutoDiscovererTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Test
    public void exceptionIsThrown_whenThereIsNoRuntimeEnvironmentDefined() throws Exception {
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(null);

        assertThatExceptionOfType(AutoDiscovererException.class).isThrownBy(() -> createDiscoverer(robotProject))
                .withMessage(String.format("There is no active runtime environment for project '%s'", PROJECT_NAME))
                .withNoCause();
    }

    private AbstractAutoDiscoverer createDiscoverer(final RobotProject robotProject) {
        return new AbstractAutoDiscoverer(robotProject) {

            @Override
            public Job start() {
                return new WorkspaceJob("Discovering") {

                    @Override
                    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                        return Status.OK_STATUS;
                    }
                };
            }

            @Override
            public void startDiscovering(final IProgressMonitor monitor) throws InterruptedException, CoreException {
                // nothing to implement
            }

            @Override
            RobotDefaultAgentEventListener createDryRunCollectorEventListener(final Consumer<String> libNameHandler) {
                return new RobotDefaultAgentEventListener() {
                    // nothing to implement
                };
            }

            @Override
            void startDryRunClient(final int port, final String dataSourcePath) throws CoreException {
                // nothing to implement
            }
        };
    }

}
