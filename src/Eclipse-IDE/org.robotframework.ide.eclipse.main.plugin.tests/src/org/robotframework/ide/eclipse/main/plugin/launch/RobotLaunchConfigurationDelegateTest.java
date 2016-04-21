/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
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
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RelativeTo;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RelativityPoint;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.Lists;

public class RobotLaunchConfigurationDelegateTest {

    private static final String PROJECT_NAME = RobotLaunchConfigurationDelegateTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @BeforeClass
    public static void before() throws Exception {
        projectProvider.createDir(Path.fromPortableString("001__suites_a"));
        projectProvider.createFile(Path.fromPortableString("001__suites_a/s1.robot"), 
                "*** Test Cases ***",
                "001__case1", 
                "  Log  10", 
                "001__case2", 
                "  Log  20");
    }

    @Test
    public void commandLineTranslatesSuitesNames_whenNamesContainsDoubleUnderscores() throws Exception {
        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final Collection<IResource> suiteResources = Lists
                .<IResource> newArrayList(projectProvider.getDir(Path.fromPortableString("001__suites_a")));
        final RobotLaunchConfigurationMock robotConfig = new RobotLaunchConfigurationMock(PROJECT_NAME);
        robotConfig.addSuite("001__suites_a", new ArrayList<String>());

        final RobotLaunchConfigurationDelegate delegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = delegate.createStandardModeCmd(robotConfig, robotProject, suiteResources,
                false);

        assertThat(commandLine.getCommandLine()).containsSubsequence("-s", PROJECT_NAME + ".Suites_a");
    }

    @Test
    public void commandLineDoesNotTranslateTestNames_whenNamesContainsDoubleUnderscores()
            throws Exception {

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final Collection<IResource> suiteResources = Lists
                .<IResource> newArrayList(projectProvider.getDir(Path.fromPortableString("001__suites_a")));
        final RobotLaunchConfigurationMock launchConfig = new RobotLaunchConfigurationMock(PROJECT_NAME);
        launchConfig.addSuite("001__suites_a", newArrayList("001__case1"));

        final RobotLaunchConfigurationDelegate delegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = delegate.createStandardModeCmd(launchConfig, robotProject, suiteResources,
                false);

        assertThat(commandLine.getCommandLine()).containsSubsequence("-s", PROJECT_NAME + ".Suites_a");
        assertThat(commandLine.getCommandLine()).containsSubsequence("-t", PROJECT_NAME + ".Suites_a.001__case1");
    }

    @Test
    public void commandLineContainsPythonPathsDefinedInRedXml_1() throws Exception {
        final SearchPath searchPath1 = SearchPath.create("folder1");
        final SearchPath searchPath2 = SearchPath.create("folder2");
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addPythonPath(searchPath1);
        config.addPythonPath(searchPath2);
        config.setRelativityPoint(new RelativityPoint(RelativeTo.PROJECT));
        projectProvider.addRobotNature();
        projectProvider.configure(config);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfigurationDelegate delegate = new RobotLaunchConfigurationDelegate();
        final RobotLaunchConfigurationMock launchConfig = new RobotLaunchConfigurationMock(PROJECT_NAME);
        final Collection<IResource> suiteResources = Lists
                .<IResource> newArrayList(projectProvider.getDir(Path.fromPortableString("001__suites_a")));
        
        final RunCommandLine commandLine = delegate.createStandardModeCmd(launchConfig, robotProject, suiteResources,
                false);

        final String projectAbsPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).containsSubsequence("-P",
                projectAbsPath + File.separator + "folder1:" + projectAbsPath + File.separator + "folder2");
    }

    @Test
    public void commandLineContainsPythonPathsDefinedInRedXml_2() throws Exception {
        final SearchPath searchPath1 = SearchPath.create(PROJECT_NAME + "/folder1");
        final SearchPath searchPath2 = SearchPath.create(PROJECT_NAME + "/folder2");
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addPythonPath(searchPath1);
        config.addPythonPath(searchPath2);
        config.setRelativityPoint(new RelativityPoint(RelativeTo.WORKSPACE));
        projectProvider.addRobotNature();
        projectProvider.configure(config);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfigurationDelegate delegate = new RobotLaunchConfigurationDelegate();
        final RobotLaunchConfigurationMock launchConfig = new RobotLaunchConfigurationMock(PROJECT_NAME);
        final Collection<IResource> suiteResources = Lists
                .<IResource> newArrayList(projectProvider.getDir(Path.fromPortableString("001__suites_a")));

        final RunCommandLine commandLine = delegate.createStandardModeCmd(launchConfig, robotProject, suiteResources,
                false);

        final String projectAbsPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getCommandLine()).containsSubsequence("-P",
                projectAbsPath + File.separator + "folder1:" + projectAbsPath + File.separator + "folder2");
    }
}
