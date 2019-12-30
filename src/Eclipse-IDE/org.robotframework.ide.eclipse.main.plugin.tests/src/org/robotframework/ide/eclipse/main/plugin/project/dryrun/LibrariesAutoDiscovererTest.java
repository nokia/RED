/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExecutionEnvironment;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer.DiscovererFactory;
import org.robotframework.red.junit.ProjectProvider;

@RunWith(MockitoJUnitRunner.class)
public class LibrariesAutoDiscovererTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(LibrariesAutoDiscovererTest.class);

    @ClassRule
    public static ProjectProvider secondProjectProvider = new ProjectProvider("SECOND_PROJECT");

    @Mock
    private DiscovererFactory factory;

    @Mock
    private LibrariesAutoDiscoverer discoverer;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("suite.robot");
        projectProvider.createFile("resource.robot");
        secondProjectProvider.createFile("secondSuite.robot");
    }

    @Before
    public void before() throws Exception {
        projectProvider.configure();
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
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.getFile("suite.robot"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.getFile("resource.robot"));
        LibrariesAutoDiscoverer.start(newArrayList(suite1, suite2), factory);

        verify(factory).create(model.createRobotProject(projectProvider.getProject()), newArrayList(suite1, suite2));
        verifyNoMoreInteractions(factory);
        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);
    }

    @Test
    public void discoveringIsStartedOnlyOnce_whenSuitesFromSeveralProjectsAreProvided() throws Exception {
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.getFile("suite.robot"));
        final RobotSuiteFile suite2 = model.createSuiteFile(secondProjectProvider.getFile("secondSuite.robot"));
        LibrariesAutoDiscoverer.start(newArrayList(suite1, suite2), factory);

        verify(factory).create(model.createRobotProject(projectProvider.getProject()), newArrayList(suite1));
        verifyNoMoreInteractions(factory);
        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);
    }

    @Test
    public void discoveringIsNotStarted_whenRuntimeEnvironmentIsInvalid() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setExecutionEnvironment(ExecutionEnvironment.create("", null));
        projectProvider.configure(config);

        final RobotModel model = new RobotModel();
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.getFile("suite.robot"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.getFile("resource.robot"));
        LibrariesAutoDiscoverer.start(newArrayList(suite1, suite2), factory);

        verifyNoInteractions(factory);
        verifyNoInteractions(discoverer);
    }
}
