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
import static org.robotframework.red.junit.jupiter.ProjectExtension.createDir;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.InvalidPythonRuntimeEnvironment;
import org.rf.ide.core.environment.MissingRobotRuntimeEnvironment;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.remote.RemoteRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationTabValidator.LaunchConfigurationValidationException;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationTabValidator.LaunchConfigurationValidationFatalException;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.jupiter.LaunchConfig;
import org.robotframework.red.junit.jupiter.LaunchConfigExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

import com.google.common.collect.ImmutableMap;

@ExtendWith({ ProjectExtension.class, LaunchConfigExtension.class })
public class LaunchConfigurationTabValidatorTest {

    @Project
    IProject project;

    @LaunchConfig(typeId = RobotLaunchConfiguration.TYPE_ID, name = "robot")
    ILaunchConfiguration launchCfg;

    @LaunchConfig(typeId = RemoteRobotLaunchConfiguration.TYPE_ID, name = "remote")
    ILaunchConfiguration remoteLaunchCfg;

    private final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator();

    @BeforeEach
    public void beforeTest() throws Exception {
        RedPlugin.getModelManager()
                .createProject(project)
                .setRobotParserComplianceVersion(new RobotVersion(3, 1));
    }

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
        project.delete(true, null);

        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessage("Project '" + project.getName() + "' cannot be found in workspace")
                .withNoCause();
    }

    @Test
    public void whenProjectIsClosed_fatalExceptionIsThrown() throws Exception {
        project.close(null);

        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessage("Project '" + project.getName() + "' is currently closed")
                .withNoCause();
    }

    @Test
    public void whenThereAreNoSuitesSpecified_warningExceptionIsThrown() throws Exception {
        final RobotModel model = createRobotModel(new RobotRuntimeEnvironment(null, "RF 3.0"));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());

        assertThatExceptionOfType(LaunchConfigurationValidationException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessage(
                        "There are no suites specified. All suites in '" + project.getName() + "' will be executed.")
                .withNoCause();
    }

    @Test
    public void whenThereAreFolderSuitesSpecifiedTogetherWithSelectedCases_warningExceptionIsThrown() throws Exception {
        createFile(project, "fileWithCases.robot", "*** Test Cases ***", "case1", " Log 10", "case2", " Log 20");
        createDir(project, "suiteFolder");
        createFile(project, "suiteFolder/nested.robot", "*** Test Cases ***", "case", " Log 30");

        final RobotModel model = createRobotModel(new RobotRuntimeEnvironment(null, "RF 3.0"));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setSuitePaths(
                ImmutableMap.of("suiteFolder", newArrayList(), "fileWithCases.robot", newArrayList("case1")));

        assertThatExceptionOfType(LaunchConfigurationValidationException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessage("There are suite folders and single cases from suite files specified. "
                        + "Only single cases will be executed.")
                .withNoCause();
    }

    @Test
    public void whenSuitesSpecifiedToRunDoNotExist_fatalExceptionIsThrown() throws Exception, CoreException {
        createFile(project, "file.robot", "*** Test Cases ***", "case1", " Log 10");

        final RobotModel model = createRobotModel(new RobotRuntimeEnvironment(null, "RF 3.0"));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setSuitePaths(ImmutableMap.of("file.robot", newArrayList(), "file2.robot", newArrayList(),
                "suite/dir", newArrayList()));

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessageStartingWith("Following suites do not exist: ")
                .withMessageContaining(project.getName() + "/file2.robot")
                .withMessageContaining(project.getName() + "/suite/dir")
                .withNoCause();
    }

    @Test
    public void whenTestsSpecifiedInSuiteDoNotExist_fatalExceptionIsThrown() throws Exception, CoreException {
        createFile(project, "testSuite.robot", "*** Test Cases ***", "case1", "  Log  10", "case2", "  Log  20",
                "case3", "  Log  30");

        final RobotModel model = createRobotModel(new RobotRuntimeEnvironment(null, "RF 3.0"));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setSuitePaths(ImmutableMap.of("testSuite.robot", newArrayList("case3", "case4", "case5")));

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessage("Following cases do not exist: case4, case5")
                .withNoCause();
    }

    @Test
    public void whenTasksSpecifiedInSuiteDoNotExist_fatalExceptionIsThrown() throws Exception, CoreException {
        createFile(project, "taskSuite.robot", "*** Tasks ***", "task1", "  Log  10", "task2", "  Log  20",
                "task3", "  Log  30");

        final RobotModel model = createRobotModel(new RobotRuntimeEnvironment(null, "RF 3.1"));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setSuitePaths(ImmutableMap.of("taskSuite.robot", newArrayList("task3", "task4", "task5")));

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessage("Following cases do not exist: task4, task5")
                .withNoCause();
    }

    @Test
    public void whenSuitesSpecifiedToRunDoNotContainCases_fatalExceptionIsThrown() throws Exception, CoreException {
        createFile(project, "notEmpty.robot", "*** Test Cases ***", "case1", " Log 10");
        createFile(project, "noCases.robot", "*** Test Cases ***");
        createFile(project, "resource.robot", "*** Keywords ***", "keyword");

        final RobotModel model = createRobotModel(new RobotRuntimeEnvironment(null, "RF 3.0"));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setSuitePaths(ImmutableMap.of("notEmpty.robot", newArrayList(), "noCases.robot", newArrayList(),
                "resource.robot", newArrayList()));

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessageStartingWith("Following suites do not contain cases: ")
                .withMessageContaining(project.getName() + "/noCases.robot")
                .withMessageContaining(project.getName() + "/resource.robot")
                .withNoCause();
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithRobotTab() throws Exception, CoreException {
        createFile(project, "correctSuite.robot", "*** Test Cases ***", "case1", "  Log  10", "case2",
                "  Log  20", "case3", "  Log  30");

        final RobotModel model = createRobotModel(new RobotRuntimeEnvironment(null, "RF 3.0"));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setSuitePaths(ImmutableMap.of("correctSuite.robot", newArrayList("case2", "case3")));
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
        final RemoteRobotLaunchConfiguration launchConfig = createRemoteRobotLaunchConfiguration(project.getName());
        launchConfig.setAgentConnectionPortValue("xyz");

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateListenerTab(launchConfig))
                .withMessage("Server port 'xyz' must be an Integer between 1 and %,d", 65_535)
                .withNoCause();
    }

    @Test
    public void whenTimeoutIsNotCorrect_fatalExceptionIsThrown() throws Exception {
        final RemoteRobotLaunchConfiguration launchConfig = createRemoteRobotLaunchConfiguration(project.getName());
        launchConfig.setAgentConnectionTimeoutValue("xyz");

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateListenerTab(launchConfig))
                .withMessage("Connection timeout 'xyz' must be an Integer between 1 and %,d", 3_600)
                .withNoCause();
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithListenerTab() throws Exception {
        validator.validateListenerTab(createRemoteRobotLaunchConfiguration(project.getName()));
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithListenerTab_1() throws Exception {
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
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
        createFile(project, "file.robot", "*** Test Cases ***", "case1", "  Log  10");

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator();
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
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
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setExecutableFilePath("/not/existing/path");

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateExecutorTab(launchConfig))
                .withMessage("Executable file does not exist")
                .withNoCause();
    }

    @Test
    public void whenExecutableFileDefinedWithVariableDoesNotExist_fatalExceptionIsThrown() throws Exception {
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
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
        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(new NullRuntimeEnvironment());

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateExecutorTab(launchConfig))
                .withMessage("Project '" + project.getName() + "' uses invalid Python environment")
                .withNoCause();
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_2() throws Exception {
        final RobotModel model = createRobotModel(new InvalidPythonRuntimeEnvironment(null));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateExecutorTab(launchConfig))
                .withMessage("Project '" + project.getName() + "' uses invalid Python environment")
                .withNoCause();
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_3() throws Exception {
        final RobotModel model = createRobotModel(new MissingRobotRuntimeEnvironment(null));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateExecutorTab(launchConfig))
                .withMessage(
                        "Project '" + project.getName() + "' uses invalid Python environment (missing Robot Framework)")
                .withNoCause();
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithExecutorTab() throws Exception {
        final IFile executableFile = createFile(project, "robot_executable_file.txt", "run robot command");

        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setExecutableFilePath(executableFile.getLocation().toOSString());
        validator.validateExecutorTab(launchConfig);
    }

    @Test
    public void nothingIsThrown_whenVariableIsUsedInExecutableFilePath() throws Exception {
        createFile(project, "robot_executable_file.txt", "run robot command");

        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setExecutableFilePath("${workspace_loc:/" + project.getName() + "/robot_executable_file.txt}");
        validator.validateExecutorTab(launchConfig);
    }

    @Test
    public void nothingIsThrown_whenProjectIsUsingInvalidEnvironmentButExecutableFileIsSet() throws Exception {
        final IFile executableFile = createFile(project, "robot_executable_file.txt", "run robot command");

        final RobotModel model = createRobotModel(new MissingRobotRuntimeEnvironment(null));

        final LaunchConfigurationTabValidator validator = new LaunchConfigurationTabValidator(model);
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setExecutableFilePath(executableFile.getLocation().toOSString());
        validator.validateExecutorTab(launchConfig);
    }

    @Test
    public void whenRobotAdditionalArgumentsContainTabulators_fatalExceptionIsThrown() throws Exception {
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setRobotArguments("some\t\t srg\tuments");

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateRobotTab(launchConfig))
                .withMessage("Tabulators are not allowed in arguments")
                .withNoCause();
    }

    @Test
    public void whenInterpreterAdditionalArgumentsContainTabulators_fatalExceptionIsThrown() throws Exception {
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setInterpreterArguments("some\t\t srg\tuments");

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateExecutorTab(launchConfig))
                .withMessage("Tabulators are not allowed in arguments")
                .withNoCause();
    }

    @Test
    public void whenExecutableFileAdditionalArgumentsContainTabulators_fatalExceptionIsThrown() throws Exception {
        final RobotLaunchConfiguration launchConfig = createRobotLaunchConfiguration(project.getName());
        launchConfig.setExecutableFileArguments("some\t\t srg\tuments");

        assertThatExceptionOfType(LaunchConfigurationValidationFatalException.class)
                .isThrownBy(() -> validator.validateExecutorTab(launchConfig))
                .withMessage("Tabulators are not allowed in arguments")
                .withNoCause();
    }

    private RobotLaunchConfiguration createRobotLaunchConfiguration(final String projectName) throws CoreException {
        final RobotLaunchConfiguration launchConfig = new RobotLaunchConfiguration(launchCfg);
        launchConfig.fillDefaults();
        launchConfig.setProjectName(projectName);
        return launchConfig;
    }

    private RemoteRobotLaunchConfiguration createRemoteRobotLaunchConfiguration(final String projectName)
            throws CoreException {
        final RemoteRobotLaunchConfiguration launchConfig = new RemoteRobotLaunchConfiguration(remoteLaunchCfg);
        launchConfig.fillDefaults();
        launchConfig.setProjectName(projectName);
        return launchConfig;
    }

    private RobotModel createRobotModel(final IRuntimeEnvironment environment) {
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(project)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);
        return model;
    }

}
