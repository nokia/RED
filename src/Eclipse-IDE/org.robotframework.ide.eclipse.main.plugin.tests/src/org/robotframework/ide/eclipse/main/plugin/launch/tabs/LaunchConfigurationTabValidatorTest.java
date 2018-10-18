/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.remote.RemoteRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationTabValidator.LaunchConfigurationValidationException;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationTabValidator.LaunchConfigurationValidationFatalException;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RuntimeEnvironmentsMocks;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.RunConfigurationProvider;

import com.google.common.collect.ImmutableMap;

public class LaunchConfigurationTabValidatorTest {

    private static final String PROJECT_NAME = LaunchConfigurationTabValidatorTest.class.getSimpleName();

    private final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator();

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public RunConfigurationProvider robotRunConfigurationProvider = new RunConfigurationProvider(
            RobotLaunchConfiguration.TYPE_ID);

    @Rule
    public RunConfigurationProvider remoteRobotRunConfigurationProvider = new RunConfigurationProvider(
            RemoteRobotLaunchConfiguration.TYPE_ID);

    @Test
    public void whenProjectNameIsEmpty_fatalExceptionIsThrown() throws Exception {
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration("");

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessage("Project cannot be empty")
                .withNoCause();
    }

    @Test
    public void whenProjectDoesNotExist_fatalExceptionIsThrown() throws Exception {
        projectProvider.getProject().delete(true, null);

        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessage("Project '" + PROJECT_NAME + "' cannot be found in workspace")
                .withNoCause();
    }

    @Test
    public void whenProjectIsClosed_fatalExceptionIsThrown() throws Exception {
        projectProvider.getProject().close(null);

        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessage("Project '" + PROJECT_NAME + "' is currently closed")
                .withNoCause();
    }

    @Test
    public void whenThereAreNoSuitesSpecified_warningExceptionIsThrown() throws Exception {
        final RobotModel model = createRobotModel(RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0"));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        assertThatExceptionOfType(LaunchConfigurationValidationException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessage("There are no suites specified. All suites in '" + PROJECT_NAME + "' will be executed.")
                .withNoCause();
    }

    @Test
    public void whenSuitesSpecifiedToRunDoesNotExist_fatalExceptionIsThrown() throws Exception, CoreException {
        projectProvider.createFile("file.robot", "*** Test Cases ***", "case1", " Log 10");

        final RobotModel model = createRobotModel(RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0"));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setSuitePaths(ImmutableMap.of("file.robot", newArrayList(), "file2.robot", newArrayList(),
                "suite/dir", newArrayList()));

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessageStartingWith("Following suites do not exist: ")
                .withMessageContaining(PROJECT_NAME + "/file2.robot")
                .withMessageContaining(PROJECT_NAME + "/suite/dir")
                .withNoCause();
    }

    @Test
    public void whenTestsSpecifiedInSuiteDoNotExist_fatalExceptionIsThrown() throws Exception, CoreException {
        projectProvider.createFile("file.robot", "*** Test Cases ***", "case1", "  Log  10", "case2", "  Log  20",
                "case3", "  Log  30");

        final RobotModel model = createRobotModel(RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0"));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setSuitePaths(ImmutableMap.of("file.robot", newArrayList("case3", "case4", "case5")));

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessage("Following tests do not exist: case4, case5")
                .withNoCause();
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithRobotTab() throws Exception, CoreException {
        projectProvider.createFile("file.robot", "*** Test Cases ***", "case1", "  Log  10", "case2", "  Log  20",
                "case3", "  Log  30");

        final RobotModel model = createRobotModel(RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0"));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setSuitePaths(ImmutableMap.of("file.robot", newArrayList("case2", "case3")));
        validator.validateRobotTab(launchConfig);
    }

    @Test
    public void whenRemoteProjectNameIsEmpty_fatalExceptionIsThrown() throws Exception {
        final RemoteRobotLaunchConfiguration launchConfig = createRemoteRobotLaunchConfiguration("");

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateListenerTab(launchConfig))
                .withMessage("Project cannot be empty")
                .withNoCause();
    }

    @Test
    public void whenPortIsNotCorrect_fatalExceptionIsThrown() throws Exception {
        final RemoteRobotLaunchConfiguration launchConfig = createRemoteRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setAgentConnectionPortValue("xyz");

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateListenerTab(launchConfig))
                .withMessage("Server port 'xyz' must be an Integer between 1 and %,d", 65_535)
                .withNoCause();
    }

    @Test
    public void whenTimeoutIsNotCorrect_fatalExceptionIsThrown() throws Exception {
        final RemoteRobotLaunchConfiguration launchConfig = createRemoteRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setAgentConnectionTimeoutValue("xyz");

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateListenerTab(launchConfig))
                .withMessage("Connection timeout 'xyz' must be an Integer between 1 and %,d", 3_600)
                .withNoCause();
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithListenerTab() throws Exception {
        validator.validateListenerTab(createRemoteRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithListenerTab_1() throws Exception {
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setUsingRemoteAgent(true);
        launchConfig.setAgentConnectionHostValue("1.2.3.4");
        launchConfig.setAgentConnectionPortValue("12345");
        launchConfig.setAgentConnectionTimeoutValue("99");
        validator.validateListenerTab(launchConfig);
    }

    @Test
    public void projectIsNotValidated_whenNotDefinedDirectly() throws Exception {
        validator.validateListenerTab(createRobotLaunchConfiguration(""));
    }

    @Test
    public void whenSystemInterpreterIsUsed_warningExceptionIsThrown() throws Exception {
        projectProvider.createFile("file.robot", "*** Test Cases ***", "case1", "  Log  10");

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator();
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setUsingInterpreterFromProject(false);
        launchConfig.setSuitePaths(ImmutableMap.of("file.robot", newArrayList()));

        assertThatExceptionOfType(LaunchConfigurationValidationException.class)
                .isThrownBy(() -> validator.validateExecutorTab(launchConfig))
                .withMessage(
                        "Tests will be launched using 'Python' interpreter as defined in PATH environment variable")
                .withNoCause();
    }

    @Test
    public void whenExecutableFileDoesNotExist_fatalExceptionIsThrown() throws Exception {
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setExecutableFilePath("/not/existing/path");

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateExecutorTab(launchConfig))
                .withMessage("Executable file does not exist")
                .withNoCause();
    }

    @Test
    public void whenExecutableFileDefinedWithVariableDoesNotExist_fatalExceptionIsThrown() throws Exception {
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setExecutableFilePath("${workspace_loc:/not_existing.txt}");

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateExecutorTab(launchConfig))
                .withMessage("Executable file does not exist")
                .withCauseExactlyInstanceOf(CoreException.class);
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_1() throws Exception {
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(projectProvider.getProject())).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(null);

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateExecutorTab(launchConfig))
                .withMessage("Project '" + PROJECT_NAME + "' is using invalid Python environment")
                .withNoCause();
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_2() throws Exception {
        final RobotModel model = createRobotModel(RuntimeEnvironmentsMocks.createInvalidPythonEnvironment());

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateExecutorTab(launchConfig))
                .withMessage("Project '" + PROJECT_NAME + "' is using invalid Python environment")
                .withNoCause();
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_3() throws Exception {
        final RobotModel model = createRobotModel(RuntimeEnvironmentsMocks.createInvalidRobotEnvironment());

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateExecutorTab(launchConfig))
                .withMessage(
                        "Project '" + PROJECT_NAME + "' is using invalid Python environment (missing Robot Framework)")
                .withNoCause();
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithExecutorTab() throws Exception {
        final IFile executableFile = projectProvider.createFile("robot_executable_file.txt", "run robot command");

        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setExecutableFilePath(executableFile.getLocation().toOSString());
        validator.validateExecutorTab(launchConfig);
    }

    @Test
    public void nothingIsThrown_whenVariableIsUsedInExecutableFilePath() throws Exception {
        projectProvider.createFile("robot_executable_file.txt", "run robot command");

        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setExecutableFilePath("${workspace_loc:/" + PROJECT_NAME + "/robot_executable_file.txt}");
        validator.validateExecutorTab(launchConfig);
    }

    @Test
    public void nothingIsThrown_whenProjectIsUsingInvalidEnvironmentButExecutableFileIsSet() throws Exception {
        final IFile executableFile = projectProvider.createFile("robot_executable_file.txt", "run robot command");

        final RobotModel model = createRobotModel(RuntimeEnvironmentsMocks.createInvalidRobotEnvironment());

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setExecutableFilePath(executableFile.getLocation().toOSString());
        validator.validateExecutorTab(launchConfig);
    }

    private RobotLaunchConfiguration createRobotLaunchConfiguration(final String projectName) throws CoreException {
        final ILaunchConfiguration configuration = robotRunConfigurationProvider.create("robot");
        final RobotLaunchConfiguration launchConfig = new RobotLaunchConfiguration(configuration);
        launchConfig.fillDefaults();
        launchConfig.setProjectName(projectName);
        return launchConfig;
    }

    private RemoteRobotLaunchConfiguration createRemoteRobotLaunchConfiguration(final String projectName)
            throws CoreException {
        final ILaunchConfiguration configuration = remoteRobotRunConfigurationProvider.create("remote");
        final RemoteRobotLaunchConfiguration launchConfig = new RemoteRobotLaunchConfiguration(configuration);
        launchConfig.fillDefaults();
        launchConfig.setProjectName(projectName);
        return launchConfig;
    }

    private RobotModel createRobotModel(final RobotRuntimeEnvironment environment) {
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(projectProvider.getProject())).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);
        return model;
    }

}
