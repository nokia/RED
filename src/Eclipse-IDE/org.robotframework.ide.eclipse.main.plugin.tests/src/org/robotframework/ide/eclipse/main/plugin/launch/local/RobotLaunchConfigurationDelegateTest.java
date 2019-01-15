/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.environment.InvalidPythonRuntimeEnvironment;
import org.rf.ide.core.environment.MissingRobotRuntimeEnvironment;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.rf.ide.core.environment.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfigurationDelegate.ConsoleData;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.RunConfigurationProvider;

public class RobotLaunchConfigurationDelegateTest {

    private static final String PROJECT_NAME = RobotLaunchConfigurationDelegateTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public RunConfigurationProvider runConfigurationProvider = new RunConfigurationProvider(
            RobotLaunchConfiguration.TYPE_ID);

    @Test()
    public void pathToExecutableAndUnknownRobotVersionAreUsed_whenPathToExecutableIsSet() throws Exception {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setExecutableFilePath("some/path/to/script");

        final ConsoleData consoleData = RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject);

        assertThat(consoleData.getProcessLabel()).isEqualTo("some/path/to/script");
        assertThat(consoleData.getSuiteExecutorVersion()).isEqualTo("<unknown>");
    }

    @Test()
    public void pathToPythonAndKnownRobotVersionAreUsed_whenProjectInterpreterIsNotUsedAndPathToExecutableIsNotSet()
            throws Exception {
        assumeTrue(PythonInstallationDirectoryFinder.whereIsPythonInterpreter(SuiteExecutor.Python).isPresent());

        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setUsingInterpreterFromProject(false);
        robotConfig.setInterpreter(SuiteExecutor.Python);

        final ConsoleData consoleData = RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject);

        assertThat(consoleData.getProcessLabel()).endsWith(SuiteExecutor.Python.executableName());
        assertThat(consoleData.getSuiteExecutorVersion()).isNotEqualTo("<unknown>");
    }

    @Test()
    public void coreExceptionIsThrown_whenNotExistingInterpreterIsUsedAndPathToExecutableIsNotSet() throws Exception {
        assumeFalse(PythonInstallationDirectoryFinder.whereIsPythonInterpreter(SuiteExecutor.PyPy).isPresent());

        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setUsingInterpreterFromProject(false);
        robotConfig.setInterpreter(SuiteExecutor.PyPy);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject))
                .withMessage("There is no %s interpreter in system PATH environment variable",
                        SuiteExecutor.PyPy.name())
                .withNoCause();
    }

    @Test()
    public void pythonExecNameAndKnownRobotVersionAreUsed_whenProjectInterpreterIsUsedAndPathToExecutableIsNotSet()
            throws Exception {
        final PythonInstallationDirectory location = mock(PythonInstallationDirectory.class);
        when(location.listFiles()).thenReturn(new File[] {});
        when(location.getInterpreter()).thenReturn(SuiteExecutor.Python);
        final RobotRuntimeEnvironment environment = new RobotRuntimeEnvironment(location, "RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        final ConsoleData consoleData = RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject);

        assertThat(consoleData.getProcessLabel()).isEqualTo(SuiteExecutor.Python.executableName());
        assertThat(consoleData.getSuiteExecutorVersion()).isEqualTo("RF 3");
    }

    @Test()
    public void coreExceptionIsThrown_whenActiveRuntimeEnvironmentIsNullEnvironment() throws Exception {
        final RobotRuntimeEnvironment environment = new NullRuntimeEnvironment();
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject))
                .withMessage("There is no active runtime environment for project '%s'", PROJECT_NAME)
                .withNoCause();
    }

    @Test()
    public void coreExceptionIsThrown_whenActiveRuntimeEnvironmentIsInvalidPythonEnvironment() throws Exception {
        final File location = new File("some/path");
        final RobotRuntimeEnvironment environment = new InvalidPythonRuntimeEnvironment(location);
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject))
                .withMessage("The runtime environment %s is invalid Python installation", location.getAbsolutePath())
                .withNoCause();
    }

    @Test()
    public void coreExceptionIsThrown_whenActiveRuntimeEnvironmentIsInvalidRobotEnvironment() throws Exception {
        final File location = new File("some/path/to/python");
        final RobotRuntimeEnvironment environment = new MissingRobotRuntimeEnvironment(location);
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(projectProvider.getProject()));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig, robotProject))
                .withMessage("The runtime environment %s has no Robot Framework installed", location.getAbsolutePath())
                .withNoCause();
    }

    private RobotLaunchConfiguration createRobotLaunchConfiguration(final String projectName) throws CoreException {
        final ILaunchConfiguration configuration = runConfigurationProvider.create("robot");
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(projectName);
        return robotConfig;
    }

    @Test
    public void whenConfigurationVersionIsInvalid_coreExceptionIsThrown() throws Exception {
        final ILaunchConfiguration configuration = runConfigurationProvider.create("robot");
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(PROJECT_NAME);

        final ILaunchConfigurationWorkingCopy launchCopy = configuration.getWorkingCopy();
        launchCopy.setAttribute("Version of configuration", "invalid");

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> launchDelegate.launch(launchCopy, "run", null, null))
                .withMessage(
                        "This configuration is incompatible with RED version you are currently using.%n"
                                + "Expected: %s, but was: %s"
                                + "%n%nResolution: Delete old configurations manually and create the new ones.",
                        RobotLaunchConfiguration.CURRENT_CONFIGURATION_VERSION, "invalid")
                .withNoCause();
    }
}
