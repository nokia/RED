/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.AbstractAutoDiscoverer.IDryRunTargetsCollector;
import org.robotframework.red.junit.ProjectProvider;

public class AbstractAutoDiscovererTest {

    private static final String PROJECT_NAME = AbstractAutoDiscovererTest.class.getSimpleName();

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void exceptionIsThrown_whenThereIsNoRuntimeEnvironmentDefined() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage(String.format("There is no active runtime environment for project '%s'", PROJECT_NAME));

        final RobotRuntimeEnvironment environment = null;
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final AbstractAutoDiscoverer discoverer = createDiscoverer(robotProject);

        discoverer.startDiscovering(null);
    }

    private AbstractAutoDiscoverer createDiscoverer(final RobotProject robotProject) {
        return new AbstractAutoDiscoverer(robotProject, Arrays.asList(), createTargetCollector()) {

            @Override
            RobotDefaultAgentEventListener createDryRunEventListener(final Consumer<String> startSuiteHandler) {
                return new RobotDefaultAgentEventListener() {
                    // nothing to implement
                };
            }

            @Override
            void start(final Shell parent) {
                // nothing to do
            }
        };
    }

    private IDryRunTargetsCollector createTargetCollector() {
        return new IDryRunTargetsCollector() {

            @Override
            public void collectSuiteNamesAndAdditionalProjectsLocations(final RobotProject robotProject,
                    final List<? extends IResource> resources) {
                // nothing to do
            }

            @Override
            public List<String> getSuiteNames() {
                return Arrays.asList();
            }

            @Override
            public List<File> getAdditionalProjectsLocations() {
                return Arrays.asList();
            }

        };
    }

}
