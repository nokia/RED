/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.AbstractAutoDiscoverer.IDryRunTargetsCollector;
import org.robotframework.red.junit.ProjectProvider;

public class AbstractAutoDiscovererTest {

    private static final String PROJECT_NAME = AbstractAutoDiscovererTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void exceptionIsThrown_whenThereIsNoRuntimeEnvironmentDefined() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage(String.format("There is no active runtime environment for project '%s'", PROJECT_NAME));

        final RobotRuntimeEnvironment environment = null;
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final Collection<RobotSuiteFile> suites = Collections.emptyList();

        final LibrariesSourcesCollector sourcesCollector = new LibrariesSourcesCollector(robotProject);

        final IDryRunTargetsCollector targetsCollector = mock(IDryRunTargetsCollector.class);

        createDiscoverer(robotProject, suites, sourcesCollector, targetsCollector).startDiscovering(null);
    }

    private AbstractAutoDiscoverer createDiscoverer(final RobotProject robotProject,
            final Collection<RobotSuiteFile> suites, final LibrariesSourcesCollector sourcesCollector,
            final IDryRunTargetsCollector targetsCollector) {
        return new AbstractAutoDiscoverer(robotProject, suites, sourcesCollector, targetsCollector) {

            @Override
            RobotDefaultAgentEventListener createDryRunCollectorEventListener(
                    final Consumer<String> startSuiteHandler) {
                return new RobotDefaultAgentEventListener() {
                    // nothing to implement
                };
            }

            @Override
            Job start(final Shell parent) {
                return new WorkspaceJob("Discovering") {

                    @Override
                    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                        return Status.OK_STATUS;
                    }
                };
            }
        };
    }

}
