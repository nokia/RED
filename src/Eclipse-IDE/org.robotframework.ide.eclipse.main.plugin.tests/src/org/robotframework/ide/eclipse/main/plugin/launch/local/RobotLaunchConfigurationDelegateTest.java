/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.RelativeTo;
import org.rf.ide.core.project.RobotProjectConfig.RelativityPoint;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RuntimeEnvironmentsMocks;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.RunConfigurationProvider;

import com.google.common.collect.ImmutableMap;

public class RobotLaunchConfigurationDelegateTest {

    private static final String PROJECT_NAME = RobotLaunchConfigurationDelegateTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public RunConfigurationProvider runConfigurationProvider = new RunConfigurationProvider(
            RobotLaunchConfiguration.TYPE_ID);

    @BeforeClass
    public static void before() throws Exception {
        projectProvider.createDir(Path.fromPortableString("001__suites_a"));
        projectProvider.createFile(Path.fromPortableString("001__suites_a/s1.robot"), "*** Test Cases ***",
                "001__case1", "  Log  10", "001__case2", "  Log  20");
    }

    @Test
    public void commandLineTranslatesSuitesNames_whenNamesContainsDoubleUnderscores() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a", newArrayList()));

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        assertThat(commandLine.getArgumentFile().get().generateContent())
                .contains("--suite      " + PROJECT_NAME + ".Suites_a");
    }

    @Test
    public void commandLineDoesNotTranslateTestNames_whenNamesContainsDoubleUnderscores() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("001__suites_a", newArrayList("001__case1")));

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        final String argFileContent = commandLine.getArgumentFile().get().generateContent();
        assertThat(argFileContent).contains("-suite", PROJECT_NAME + ".Suites_a");
        assertThat(argFileContent).contains("-test", PROJECT_NAME + ".Suites_a.001__case1");
    }

    @Test
    public void commandLineContainsPythonPathsDefinedInRedXml_1() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final SearchPath searchPath1 = SearchPath.create("folder1");
        final SearchPath searchPath2 = SearchPath.create("folder2");
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addPythonPath(searchPath1);
        config.addPythonPath(searchPath2);
        config.setRelativityPoint(RelativityPoint.create(RelativeTo.PROJECT));
        projectProvider.configure(config);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        final String projectAbsPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getArgumentFile().get().generateContent()).contains("-pythonpath " + projectAbsPath
                + File.separator + "folder1:" + projectAbsPath + File.separator + "folder2");
    }

    @Test
    public void commandLineContainsPythonPathsDefinedInRedXml_2() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldLaunchUsingArgumentsFile()).thenReturn(true);

        final SearchPath searchPath1 = SearchPath.create(PROJECT_NAME + "/folder1");
        final SearchPath searchPath2 = SearchPath.create(PROJECT_NAME + "/folder2");
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addPythonPath(searchPath1);
        config.addPythonPath(searchPath2);
        config.setRelativityPoint(RelativityPoint.create(RelativeTo.WORKSPACE));
        projectProvider.configure(config);

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        final String projectAbsPath = projectProvider.getProject().getLocation().toOSString();
        assertThat(commandLine.getArgumentFile().get().generateContent()).contains("--pythonpath " + projectAbsPath
                + File.separator + "folder1:" + projectAbsPath + File.separator + "folder2");
    }

    @Test
    public void commandLineStartsWitExecutableFilePath() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final String executablePath = projectProvider.createFile("executable_script.bat").getLocation().toOSString();
        robotConfig.setExecutableFilePath(executablePath);

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        assertThat(commandLine.getCommandLine()).startsWith(executablePath);
    }

    @Test
    public void commandLineContainsExecutableFilePathWithArguments() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final String executablePath = projectProvider.createFile("executable_script.bat").getLocation().toOSString();
        robotConfig.setExecutableFilePath(executablePath);
        robotConfig.setExecutableFileArguments("-arg1 abc -arg2 xyz");

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        final RunCommandLine commandLine = launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345,
                preferences);

        assertThat(commandLine.getCommandLine()).containsSubsequence(executablePath, "-arg1", "abc", "-arg2", "xyz");
    }

    @Test(expected = CoreException.class)
    public void coreExceptionIsThrown_whenExecutableFileDoesNotExist() throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final String executablePath = projectProvider.getFile("not_existing.bat").getLocation().toOSString();
        robotConfig.setExecutableFilePath(executablePath);

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        launchDelegate.prepareCommandLine(robotConfig, robotProject, 12345, preferences);
    }

    private RobotLaunchConfiguration createRobotLaunchConfiguration(final String projectName) throws CoreException {
        final ILaunchConfiguration configuration = runConfigurationProvider.create("robot");
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(projectName);
        return robotConfig;
    }

    @Test(expected = CoreException.class)
    public void whenConfigurationVersionIsInvalid_coreExceptionIsThrown() throws Exception {
        final ILaunchConfiguration configuration = runConfigurationProvider.create("robot");
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(PROJECT_NAME);

        final ILaunchConfigurationWorkingCopy launchCopy = configuration.getWorkingCopy();
        launchCopy.setAttribute("Version of configuration", "invalid");

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();
        launchDelegate.launch(launchCopy, null, null, null);
    }
}
