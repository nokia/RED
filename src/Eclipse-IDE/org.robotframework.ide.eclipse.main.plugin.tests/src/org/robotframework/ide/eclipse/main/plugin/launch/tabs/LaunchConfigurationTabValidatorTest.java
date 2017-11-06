/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
    public final ExpectedException thrown = ExpectedException.none();

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
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project cannot be empty");

        validator.validateRobotTab(createRobotLaunchConfiguration(""));
    }

    @Test
    public void whenProjectDoesNotExist_fatalExceptionIsThrown() throws Exception {
        projectProvider.getProject().delete(true, null);

        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '" + PROJECT_NAME + "' cannot be found in workspace");

        validator.validateRobotTab(createRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void whenProjectIsClosed_fatalExceptionIsThrown() throws Exception {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '" + PROJECT_NAME + "' is currently closed");
        projectProvider.getProject().close(null);

        validator.validateRobotTab(createRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void whenThereAreNoSuitesSpecified_warningExceptionIsThrown() throws Exception {
        thrown.expect(LaunchConfigurationValidationException.class);
        thrown.expectMessage("There are no suites specified. All suites in '" + PROJECT_NAME + "' will be executed.");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(projectProvider.getProject())).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        validator.validateRobotTab(createRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void whenSuitesSpecifiedToRunDoesNotExist_fatalExceptionIsThrown() throws Exception, CoreException {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage(CoreMatchers
                .<String> either(CoreMatchers.<String> equalTo("Following suites does not exist: /" + PROJECT_NAME
                        + "/file2.robot, /" + PROJECT_NAME + "/suite/dir"))
                .or(CoreMatchers.<String> equalTo("Following suites does not exist: /" + PROJECT_NAME + "/suite/dir, /"
                        + PROJECT_NAME + "/file2.robot")));

        final IPath filePath = Path.fromPortableString("file.robot");
        projectProvider.createFile(filePath, "*** Test Cases ***", "case1", " Log 10");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(projectProvider.getProject())).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setSuitePaths(ImmutableMap.of(filePath.toPortableString(), newArrayList(), "file2.robot",
                newArrayList(), "suite/dir", newArrayList()));
        validator.validateRobotTab(launchConfig);
    }

    @Test
    public void whenTestsSpecifiedInSuiteDoNotExist_fatalExceptionIsThrown() throws Exception, CoreException {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage(CoreMatchers.equalTo("Following tests does not exist: case4, case5"));

        final IPath filePath = Path.fromPortableString("file.robot");
        projectProvider.createFile(filePath, "*** Test Cases ***", "case1", "  Log  10", "case2", "  Log  20", "case3",
                "  Log  30");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(projectProvider.getProject())).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig
                .setSuitePaths(ImmutableMap.of(filePath.toPortableString(), newArrayList("case3", "case4", "case5")));
        validator.validateRobotTab(launchConfig);
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithRobotTab() throws Exception, CoreException {
        final IPath filePath = Path.fromPortableString("file.robot");
        projectProvider.createFile(filePath, "*** Test Cases ***", "case1", "  Log  10", "case2", "  Log  20", "case3",
                "  Log  30");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(projectProvider.getProject())).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setSuitePaths(ImmutableMap.of(filePath.toPortableString(), newArrayList("case2", "case3")));
        validator.validateRobotTab(launchConfig);
    }

    @Test
    public void whenRemoteProjectNameIsEmpty_fatalExceptionIsThrown() throws Exception {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project cannot be empty");

        validator.validateListenerTab(createRemoteRobotLaunchConfiguration(""));
    }

    @Test
    public void whenPortIsNotCorrect_fatalExceptionIsThrown() throws CoreException {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage(String.format("Server port 'xyz' must be an Integer between 1 and %,d", 65_535));

        final RemoteRobotLaunchConfiguration launchConfig = createRemoteRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setAgentConnectionPortValue("xyz");
        validator.validateListenerTab(launchConfig);
    }

    @Test
    public void whenTimeoutIsNotCorrect_fatalExceptionIsThrown() throws CoreException {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage(String.format("Connection timeout 'xyz' must be an Integer between 1 and %,d", 3_600));

        final RemoteRobotLaunchConfiguration launchConfig = createRemoteRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setAgentConnectionTimeoutValue("xyz");
        validator.validateListenerTab(launchConfig);
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
        thrown.expect(LaunchConfigurationValidationException.class);
        thrown.expectMessage(CoreMatchers
                .equalTo("Tests will be launched using 'Python' interpreter as defined in PATH environment variable"));

        final IPath filePath = Path.fromPortableString("file.robot");
        projectProvider.createFile(filePath, "*** Test Cases ***", "case1", "  Log  10");

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator();
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setUsingInterpreterFromProject(false);
        launchConfig.setSuitePaths(ImmutableMap.of(filePath.toPortableString(), newArrayList()));
        validator.validateExecutorTab(launchConfig);
    }

    @Test
    public void whenExecutableFileDoesNotExist_fatalExceptionIsThrown() throws CoreException {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Executable file does not exist");

        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setExecutableFilePath("/not/existing/path");
        validator.validateExecutorTab(launchConfig);
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_1() throws Exception {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '" + PROJECT_NAME + "' is using invalid Python environment");

        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(projectProvider.getProject())).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(null);

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        validator.validateExecutorTab(createRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_2() throws Exception {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '" + PROJECT_NAME + "' is using invalid Python environment");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createInvalidPythonEnvironment();
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(projectProvider.getProject())).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        validator.validateExecutorTab(createRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_3() throws Exception {
        thrown.expect(LaunchConfigurationValidationFatalException.class);
        thrown.expectMessage(
                "Project '" + PROJECT_NAME + "' is using invalid Python environment (missing Robot Framework)");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createInvalidRobotEnvironment();
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(projectProvider.getProject())).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        validator.validateExecutorTab(createRobotLaunchConfiguration(PROJECT_NAME));
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithExecutorTab() throws Exception {
        final IFile executableFile = projectProvider.createFile("robot_executable_file.txt", "run robot command");

        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        launchConfig.setExecutableFilePath(executableFile.getLocation().toOSString());
        validator.validateExecutorTab(launchConfig);
    }

    @Test
    public void nothingIsThrown_whenProjectIsUsingInvalidEnvironmentButExecutableFileIsSet() throws Exception {
        final IFile executableFile = projectProvider.createFile("robot_executable_file.txt", "run robot command");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createInvalidRobotEnvironment();
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(projectProvider.getProject())).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

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

}
