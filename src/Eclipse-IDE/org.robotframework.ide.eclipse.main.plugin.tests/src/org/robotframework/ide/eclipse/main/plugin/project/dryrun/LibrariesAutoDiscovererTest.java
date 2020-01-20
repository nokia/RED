/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.configure;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExecutionEnvironment;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer.DiscovererFactory;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class LibrariesAutoDiscovererTest {

    @Project(files = { "suite.robot", "resource.robot" })
    static IProject project;

    @Project(name = "SECOND_PROJECT", files = { "secondSuite.robot" })
    static IProject secondProject;

    private DiscovererFactory factory;

    private LibrariesAutoDiscoverer discoverer;

    @BeforeEach
    public void beforeTest() throws Exception {
        configure(project);
        factory = mock(DiscovererFactory.class);
        discoverer = mock(LibrariesAutoDiscoverer.class);

        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

    }

    @Test
    public void discoveringIsNotStarted_whenSuiteListIsEmpty() throws Exception {
        LibrariesAutoDiscoverer.start(newArrayList(), factory);

        verifyNoInteractions(factory);
        verifyNoInteractions(discoverer);
    }

    @Test
    public void discoveringIsStartedOnlyOnce_whenSuitesFromSingleProjectAreProvided() throws Exception {
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suite1 = model.createSuiteFile(getFile(project, "suite.robot"));
        final RobotSuiteFile suite2 = model.createSuiteFile(getFile(project, "resource.robot"));
        LibrariesAutoDiscoverer.start(newArrayList(suite1, suite2), factory);

        verify(factory).create(model.createRobotProject(project), newArrayList(suite1, suite2));
        verifyNoMoreInteractions(factory);
        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);
    }

    @Test
    public void discoveringIsStartedOnlyOnce_whenSuitesFromSeveralProjectsAreProvided() throws Exception {
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suite1 = model.createSuiteFile(getFile(project, "suite.robot"));
        final RobotSuiteFile suite2 = model.createSuiteFile(getFile(secondProject, "secondSuite.robot"));
        LibrariesAutoDiscoverer.start(newArrayList(suite1, suite2), factory);

        verify(factory).create(model.createRobotProject(project), newArrayList(suite1));
        verifyNoMoreInteractions(factory);
        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);
    }

    @Test
    public void discoveringIsNotStarted_whenRuntimeEnvironmentIsInvalid() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setExecutionEnvironment(ExecutionEnvironment.create("", null));
        configure(project, config);

        final RobotModel model = new RobotModel();
        final RobotSuiteFile suite1 = model.createSuiteFile(getFile(project, "suite.robot"));
        final RobotSuiteFile suite2 = model.createSuiteFile(getFile(project, "resource.robot"));
        LibrariesAutoDiscoverer.start(newArrayList(suite1, suite2), factory);

        verifyNoInteractions(factory);
        verifyNoInteractions(discoverer);
    }
}
