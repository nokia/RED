/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.InvalidPythonRuntimeEnvironment;
import org.rf.ide.core.environment.MissingRobotRuntimeEnvironment;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.rf.ide.core.environment.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.jupiter.LaunchConfig;
import org.robotframework.red.junit.jupiter.LaunchConfigExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, LaunchConfigExtension.class })
public class LocalProcessInterpreterTest {

    @Project
    static IProject project;

    @LaunchConfig(typeId = RobotLaunchConfiguration.TYPE_ID, name = "robot")
    ILaunchConfiguration launchCfg;

    @Test
    public void pythonExecNameAndKnownRobotVersionAreUsed_whenExistingInterpreterIsUsed() throws Exception {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(project.getName());
        robotConfig.setUsingInterpreterFromProject(false);
        robotConfig.setInterpreter(SuiteExecutor.Python);

        final LocalProcessInterpreter interpreter = LocalProcessInterpreter.create(robotConfig, robotProject);

        assertThat(interpreter.getExecutor()).isEqualTo(SuiteExecutor.Python);
        assertThat(interpreter.getPath()).endsWith(SuiteExecutor.Python.executableName());
        assertThat(interpreter.getVersion()).isNotEqualTo("<unknown>");
    }

    @EnabledOnOs(OS.WINDOWS)
    @Test
    public void coreExceptionIsThrown_whenNotExistingInterpreterIsUsed() throws Exception {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(project.getName());
        robotConfig.setUsingInterpreterFromProject(false);
        robotConfig.setInterpreter(SuiteExecutor.Python2);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> LocalProcessInterpreter.create(robotConfig, robotProject))
                .withMessage("There is no %s interpreter in system PATH environment variable",
                        SuiteExecutor.Python2.name())
                .withNoCause();
    }

    @Test
    public void defaultPythonExecNameAndUnknownRobotVersionAreUsed_whenPathToExecutableIsSet() throws Exception {
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(project));
        when(robotProject.getRuntimeEnvironment()).thenReturn(new NullRuntimeEnvironment());

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(project.getName());
        robotConfig.setExecutableFilePath("some/path/to/script");
        robotConfig.setUsingInterpreterFromProject(true);

        final LocalProcessInterpreter interpreter = LocalProcessInterpreter.create(robotConfig, robotProject);

        assertThat(interpreter.getExecutor()).isEqualTo(SuiteExecutor.Python);
        assertThat(interpreter.getPath()).isEqualTo(SuiteExecutor.Python.executableName());
        assertThat(interpreter.getVersion()).isEqualTo("<unknown>");
    }

    @Test
    public void selectedPythonExecNameAndKnownRobotVersionAreUsed_whenPathToExecutableIsSet() throws Exception {
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(project));
        when(robotProject.getRuntimeEnvironment()).thenReturn(new NullRuntimeEnvironment());

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(project.getName());
        robotConfig.setExecutableFilePath("some/path/to/script");
        robotConfig.setUsingInterpreterFromProject(false);
        robotConfig.setInterpreter(SuiteExecutor.Python);

        final LocalProcessInterpreter interpreter = LocalProcessInterpreter.create(robotConfig, robotProject);

        assertThat(interpreter.getExecutor()).isEqualTo(SuiteExecutor.Python);
        assertThat(interpreter.getPath()).isEqualTo(SuiteExecutor.Python.executableName());
        assertThat(interpreter.getVersion()).isNotEqualTo("<unknown>");
    }

    @Test
    public void pythonExecNameAndKnownRobotVersionAreUsed_whenProjectInterpreterIsUsed()
            throws Exception {
        final PythonInstallationDirectory pythonInstallation = mock(PythonInstallationDirectory.class);
        when(pythonInstallation.listFiles()).thenReturn(new File[] {});
        when(pythonInstallation.getInterpreter()).thenReturn(SuiteExecutor.IronPython);
        final IRuntimeEnvironment environment = new RobotRuntimeEnvironment(pythonInstallation, "RF 3");
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(project));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(project.getName());
        robotConfig.setUsingInterpreterFromProject(true);

        final LocalProcessInterpreter interpreter = LocalProcessInterpreter.create(robotConfig, robotProject);

        assertThat(interpreter.getExecutor()).isEqualTo(SuiteExecutor.IronPython);
        assertThat(interpreter.getPath()).isEqualTo(SuiteExecutor.IronPython.executableName());
        assertThat(interpreter.getVersion()).isEqualTo("RF 3");
    }

    @Test
    public void coreExceptionIsThrown_whenActiveRuntimeEnvironmentIsNullEnvironment() throws Exception {
        final IRuntimeEnvironment environment = new NullRuntimeEnvironment();
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(project));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(project.getName());

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> LocalProcessInterpreter.create(robotConfig, robotProject))
                .withMessage("There is no active runtime environment for project '%s'", project.getName())
                .withNoCause();
    }

    @Test
    public void coreExceptionIsThrown_whenActiveRuntimeEnvironmentIsInvalidPythonEnvironment() throws Exception {
        final File location = new File("some/path");
        final IRuntimeEnvironment environment = new InvalidPythonRuntimeEnvironment(location);
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(project));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(project.getName());

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> LocalProcessInterpreter.create(robotConfig, robotProject))
                .withMessage("The runtime environment %s is invalid Python installation", location.getAbsolutePath())
                .withNoCause();
    }

    @Test
    public void coreExceptionIsThrown_whenActiveRuntimeEnvironmentIsInvalidRobotEnvironment() throws Exception {
        final PythonInstallationDirectory pythonInstallation = mock(PythonInstallationDirectory.class);
        when(pythonInstallation.getAbsolutePath()).thenReturn(new File("some/path/to/python").getAbsolutePath());
        final IRuntimeEnvironment environment = new MissingRobotRuntimeEnvironment(pythonInstallation);
        final RobotProject robotProject = spy(new RobotModel().createRobotProject(project));
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(project.getName());

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> LocalProcessInterpreter.create(robotConfig, robotProject))
                .withMessage("The runtime environment %s has no Robot Framework installed",
                        pythonInstallation.getAbsolutePath())
                .withNoCause();
    }

    private RobotLaunchConfiguration createRobotLaunchConfiguration(final String projectName) throws CoreException {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchCfg);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(projectName);
        return robotConfig;
    }

}
