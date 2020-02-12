/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.InvalidPythonRuntimeEnvironment;
import org.rf.ide.core.environment.MissingRobotRuntimeEnvironment;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.environment.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotEnvironments.InterpreterWithPath;

public class InstalledRobotEnvironmentsTest {

    @Test
    public void createdEnvironmentIsNullRuntimeEnvironment() {
        final InterpreterWithPath installation = new InterpreterWithPath();

        final IRuntimeEnvironment environment = InstalledRobotEnvironments.createEnvironment(installation,
                path -> newArrayList());

        assertThat(environment).isExactlyInstanceOf(NullRuntimeEnvironment.class);
        assertThat(environment.getFile()).isNull();
        assertThat(environment.getInterpreter()).isNull();
        assertThat(environment.getRobotVersion()).isEqualTo(RobotVersion.UNKNOWN);
    }

    @Test
    public void createdEnvironmentIsInvalidPythonRuntimeEnvironment() {
        final InterpreterWithPath installation = new InterpreterWithPath(null, "path/no_python");

        final IRuntimeEnvironment environment = InstalledRobotEnvironments.createEnvironment(installation,
                path -> newArrayList());

        assertThat(environment).isExactlyInstanceOf(InvalidPythonRuntimeEnvironment.class);
        assertThat(environment.getFile()).hasName("no_python");
        assertThat(environment.getInterpreter()).isNull();
        assertThat(environment.getRobotVersion()).isEqualTo(RobotVersion.UNKNOWN);
    }

    @Test
    public void createdEnvironmentIsMissingRobotRuntimeEnvironment() {
        final InterpreterWithPath installation = new InterpreterWithPath(SuiteExecutor.Python, "path/python_no_robot");

        final PythonInstallationDirectory dir1 = mock(PythonInstallationDirectory.class);
        when(dir1.getAbsolutePath()).thenReturn("path/python_no_robot");
        when(dir1.getName()).thenReturn("python_no_robot");
        when(dir1.getInterpreter()).thenReturn(SuiteExecutor.Python3);
        when(dir1.getRobotVersion()).thenReturn(Optional.empty());
        final PythonInstallationDirectory dir2 = mock(PythonInstallationDirectory.class);
        when(dir2.getAbsolutePath()).thenReturn("path/python_no_robot");
        when(dir2.getName()).thenReturn("python_no_robot");
        when(dir2.getInterpreter()).thenReturn(SuiteExecutor.Python);
        when(dir2.getRobotVersion()).thenReturn(Optional.empty());

        final IRuntimeEnvironment environment = InstalledRobotEnvironments.createEnvironment(installation,
                path -> newArrayList(dir1, dir2));

        assertThat(environment).isExactlyInstanceOf(MissingRobotRuntimeEnvironment.class);
        assertThat(environment.getFile()).hasName("python_no_robot");
        assertThat(environment.getInterpreter()).isEqualTo(SuiteExecutor.Python);
        assertThat(environment.getRobotVersion()).isEqualTo(RobotVersion.UNKNOWN);
    }

    @Test
    public void createdEnvironmentIsRobotRuntimeEnvironment() {
        final InterpreterWithPath installation = new InterpreterWithPath(SuiteExecutor.Python, "path/python_robot");

        final PythonInstallationDirectory dir1 = mock(PythonInstallationDirectory.class);
        when(dir1.getAbsolutePath()).thenReturn("path/python_robot");
        when(dir1.getName()).thenReturn("python_robot");
        when(dir1.getInterpreter()).thenReturn(SuiteExecutor.Python3);
        when(dir1.getRobotVersion()).thenReturn(Optional.of("RF 1.2.3"));
        final PythonInstallationDirectory dir2 = mock(PythonInstallationDirectory.class);
        when(dir2.getAbsolutePath()).thenReturn("path/python_robot");
        when(dir2.getName()).thenReturn("python_robot");
        when(dir2.getInterpreter()).thenReturn(SuiteExecutor.Python);
        when(dir2.getRobotVersion()).thenReturn(Optional.of("RF 4.5.6"));

        final IRuntimeEnvironment environment = InstalledRobotEnvironments.createEnvironment(installation,
                path -> newArrayList(dir1, dir2));

        assertThat(environment).isExactlyInstanceOf(RobotRuntimeEnvironment.class);
        assertThat(environment.getFile()).hasName("python_robot");
        assertThat(environment.getInterpreter()).isEqualTo(SuiteExecutor.Python);
        assertThat(environment.getRobotVersion()).isEqualTo(RobotVersion.from("RF 4.5.6"));
    }

    @Test
    public void allRuntimeEnvironmentsAreCreated() {
        final List<InterpreterWithPath> installations = newArrayList(new InterpreterWithPath(),
                new InterpreterWithPath(null, "path/no_python"),
                new InterpreterWithPath(SuiteExecutor.IronPython, "path/python_dir"),
                new InterpreterWithPath(SuiteExecutor.Python, "path/python_dir"));

        final PythonInstallationDirectory dir1 = mock(PythonInstallationDirectory.class);
        when(dir1.getAbsolutePath()).thenReturn("path/python_dir");
        when(dir1.getName()).thenReturn("python_dir");
        when(dir1.getInterpreter()).thenReturn(SuiteExecutor.IronPython);
        when(dir1.getRobotVersion()).thenReturn(Optional.empty());
        final PythonInstallationDirectory dir2 = mock(PythonInstallationDirectory.class);
        when(dir2.getAbsolutePath()).thenReturn("path/python_dir");
        when(dir1.getName()).thenReturn("python_dir");
        when(dir2.getInterpreter()).thenReturn(SuiteExecutor.Python);
        when(dir2.getRobotVersion()).thenReturn(Optional.of("RF 4.5.6"));

        final Map<InterpreterWithPath, Supplier<IRuntimeEnvironment>> environmentsMap = InstalledRobotEnvironments
                .createEnvironments(installations,
                        path -> "path/python_dir".equals(path) ? newArrayList(dir1, dir2) : newArrayList());

        assertThat(environmentsMap).hasSize(4);

        final List<IRuntimeEnvironment> environments = environmentsMap.values()
                .stream()
                .map(Supplier::get)
                .collect(Collectors.toList());
        assertThat(environments.get(0)).isExactlyInstanceOf(NullRuntimeEnvironment.class);
        assertThat(environments.get(1)).isExactlyInstanceOf(InvalidPythonRuntimeEnvironment.class);
        assertThat(environments.get(2)).isExactlyInstanceOf(MissingRobotRuntimeEnvironment.class);
        assertThat(environments.get(3)).isExactlyInstanceOf(RobotRuntimeEnvironment.class);
    }

    @Test
    void installationIsWrittenCorrectly() {
        final InterpreterWithPath installation = new InterpreterWithPath(SuiteExecutor.Python, "path/python_dir");

        final String jsonMapping = InstalledRobotEnvironments.writeInstallation(installation);

        assertThat(jsonMapping).isEqualTo("{\"interpreter\":\"Python\",\"path\":\"path/python_dir\"}");
    }

    @Test
    void installationIsReadCorrectly() {
        final String jsonMapping = "{\"interpreter\":\"Python3\",\"path\":\"path/directory\"}";

        final InterpreterWithPath installation = InstalledRobotEnvironments.readInstallation(jsonMapping);

        assertThat(installation).isEqualTo(new InterpreterWithPath(SuiteExecutor.Python3, "path/directory"));
    }

    @Test
    void nullInstallationIsReadCorrectly() {
        final InterpreterWithPath installation = InstalledRobotEnvironments.readInstallation(null);

        assertThat(installation).isEqualTo(new InterpreterWithPath());
    }

    @Test
    void emptyInstallationIsReadCorrectly() {
        final InterpreterWithPath installation = InstalledRobotEnvironments.readInstallation("");

        assertThat(installation).isEqualTo(new InterpreterWithPath());
    }

    @Test
    void installationsAreWrittenCorrectly() {
        final List<InterpreterWithPath> installations = newArrayList(new InterpreterWithPath(),
                new InterpreterWithPath(null, "path/no_python"),
                new InterpreterWithPath(SuiteExecutor.IronPython, "path/iron_python_dir"),
                new InterpreterWithPath(SuiteExecutor.Python, "path/python_dir"));

        final String jsonMapping = InstalledRobotEnvironments.writeInstallations(installations);

        assertThat(jsonMapping).isEqualTo(
                "[{\"interpreter\":null,\"path\":null},{\"interpreter\":null,\"path\":\"path/no_python\"},{\"interpreter\":\"IronPython\",\"path\":\"path/iron_python_dir\"},{\"interpreter\":\"Python\",\"path\":\"path/python_dir\"}]");
    }

    @Test
    void installationsAreReadCorrectly() {
        final String jsonMapping = "[{\"interpreter\":null,\"path\":null},{\"interpreter\":null,\"path\":\"path/invalid\"},{\"interpreter\":\"IronPython\",\"path\":\"path/iron_dir\"},{\"interpreter\":\"Jython\",\"path\":\"path/jy_dir\"}]";

        final List<InterpreterWithPath> installations = InstalledRobotEnvironments.readInstallations(jsonMapping);

        assertThat(installations).containsExactly(new InterpreterWithPath(),
                new InterpreterWithPath(null, "path/invalid"),
                new InterpreterWithPath(SuiteExecutor.IronPython, "path/iron_dir"),
                new InterpreterWithPath(SuiteExecutor.Jython, "path/jy_dir"));
    }

    @Test
    void nullInstallationsAreReadCorrectly() {
        final List<InterpreterWithPath> installations = InstalledRobotEnvironments.readInstallations(null);

        assertThat(installations).isEmpty();
    }

    @Test
    void emptyInstallationsAreReadCorrectly() {
        final List<InterpreterWithPath> installations = InstalledRobotEnvironments.readInstallations("");

        assertThat(installations).isEmpty();
    }

}
