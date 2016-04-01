/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RuntimeEnvironmentsMocks;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.Lists;

public class RobotLaunchConfigurationDelegateTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider();

    @BeforeClass
    public static void before() throws Exception {
        createProject();
    }

    @Test
    public void commandLineTranslatesSuitesNames_whenNamesContainsDoubleUnderscores() throws Exception, IOException {
        final RobotLaunchConfigurationDelegate delegate = new RobotLaunchConfigurationDelegate();

        final RobotProject robotProject = mock(RobotProject.class);
        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);
        when(robotProject.getProject()).thenReturn(projectProvider.getProject());
        when(robotProject.getClasspath()).thenReturn(new ArrayList<String>());
        when(robotProject.getPythonpath()).thenReturn(new ArrayList<String>());
        when(robotProject.getVariableFilePaths()).thenReturn(new ArrayList<String>());

        final Collection<IResource> suiteResources = Lists
                .<IResource> newArrayList(projectProvider.getDir(Path.fromPortableString("001__suites_a")));
        final RobotLaunchConfigurationMock robotConfig = new RobotLaunchConfigurationMock("Project");
        robotConfig.addSuite("001__suites_a", new ArrayList<String>());

        final RunCommandLine commandLine = delegate.createStandardModeCmd(robotConfig, robotProject, suiteResources,
                false);
        assertThat(commandLine.getCommandLine()).containsSubsequence("-s", "Project.Suites_a");
    }

    @Test
    public void commandLineDoesNotTranslateTestNames_whenNamesContainsDoubleUnderscores()
            throws Exception, IOException {
        final RobotLaunchConfigurationDelegate delegate = new RobotLaunchConfigurationDelegate();

        final RobotProject robotProject = mock(RobotProject.class);
        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);
        when(robotProject.getProject()).thenReturn(projectProvider.getProject());
        when(robotProject.getClasspath()).thenReturn(new ArrayList<String>());
        when(robotProject.getPythonpath()).thenReturn(new ArrayList<String>());
        when(robotProject.getVariableFilePaths()).thenReturn(new ArrayList<String>());

        final Collection<IResource> suiteResources = Lists
                .<IResource> newArrayList(projectProvider.getDir(Path.fromPortableString("001__suites_a")));
        final RobotLaunchConfigurationMock robotConfig = new RobotLaunchConfigurationMock("Project");
        robotConfig.addSuite("001__suites_a", newArrayList("001__case1"));

        final RunCommandLine commandLine = delegate.createStandardModeCmd(robotConfig, robotProject, suiteResources, false);
        assertThat(commandLine.getCommandLine()).containsSubsequence("-s", "Project.Suites_a");
        assertThat(commandLine.getCommandLine()).containsSubsequence("-t", "Project.Suites_a.001__case1");
    }

    private static void createProject() throws Exception {
        projectProvider.create("Project");
        projectProvider.createDir(Path.fromPortableString("001__suites_a"));
        projectProvider.createFile(Path.fromPortableString("001__suites_a/s1.robot"), 
                "*** Test Cases ***", 
                "001__case1",
                "  Log  10",
                "001__case2",
                "  Log  20");
        // projectProvider.createFile(Path.fromPortableString("suites/s2.robot"),
        // "*** Test Cases ***",
        // "case2",
        // " [Tags] tag4",
        // " Log 10");
        // projectProvider.createFile(Path.fromPortableString("s3.robot"),
        // "*** Test Cases ***",
        // "case3",
        // " [Tags] tag5",
        // " Log 10",
        // "case4",
        // " [Tags] tag4",
        // " Log 10",
        // "case5",
        // " Log 10");
    }
}
