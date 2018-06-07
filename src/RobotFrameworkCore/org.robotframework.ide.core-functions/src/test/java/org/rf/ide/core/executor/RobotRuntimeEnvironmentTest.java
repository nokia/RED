/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.executor;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rf.ide.core.RedSystemProperties;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;

public class RobotRuntimeEnvironmentTest {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void beforeSuite() throws IOException {
        folder.newFile("MoDuLe");
    }

    @Test
    public void moduleCanonicalPathIsReturned_evenWhenExecutorReturnsLowerCasePath() {
        // this test only makes sense on case-insensitive platforms like windows
        assumeTrue(RedSystemProperties.isWindowsPlatform());

        final EnvironmentSearchPaths searchPaths = new EnvironmentSearchPaths();

        final RobotCommandExecutor executor = mock(RobotCommandExecutor.class);
        when(executor.getModulePath("module", searchPaths)).thenReturn(new File(folder.getRoot(), "module"));

        final PythonInstallationDirectory location = new PythonInstallationDirectory(
                URI.create("file:///path/to/python"), SuiteExecutor.Python);

        final RobotCommandsExecutors executors = mock(RobotCommandsExecutors.class);
        when(executors.getRobotCommandExecutor(location)).thenReturn(executor);

        final RobotRuntimeEnvironment env = new RobotRuntimeEnvironment(executors, location, "3.0.0");

        final Optional<File> path = env.getModulePath("module", searchPaths);

        assertThat(path).isPresent();
        assertThat(path.get()).hasName("MoDuLe");
    }

    @Test
    public void moduleNameOfFileIsReturnedAsIs_whenExecutorReturnsItButItDoesNotExist() {
        // this test only makes sense on case-insensitive platforms like windows
        assumeTrue(RedSystemProperties.isWindowsPlatform());

        final EnvironmentSearchPaths searchPaths = new EnvironmentSearchPaths();

        final RobotCommandExecutor executor = mock(RobotCommandExecutor.class);
        when(executor.getModulePath("module2", searchPaths)).thenReturn(new File(folder.getRoot(), "module2"));

        final PythonInstallationDirectory location = new PythonInstallationDirectory(
                URI.create("file:///path/to/python"), SuiteExecutor.Python);

        final RobotCommandsExecutors executors = mock(RobotCommandsExecutors.class);
        when(executors.getRobotCommandExecutor(location)).thenReturn(executor);

        final RobotRuntimeEnvironment env = new RobotRuntimeEnvironment(executors, location, "3.0.0");

        final Optional<File> path = env.getModulePath("module2", searchPaths);

        assertThat(path).isPresent();
        assertThat(path.get()).hasName("module2");
    }

    @Test
    public void modulesSearchPathsAreCanonicalized() {
        // this test only makes sense on case-insensitive platforms like windows
        assumeTrue(RedSystemProperties.isWindowsPlatform());

        final RobotCommandExecutor executor = mock(RobotCommandExecutor.class);
        when(executor.getModulesSearchPaths())
                .thenReturn(newArrayList(new File(folder.getRoot(), "module"), new File(folder.getRoot(), "module2")));

        final PythonInstallationDirectory location = new PythonInstallationDirectory(
                URI.create("file:///path/to/python"), SuiteExecutor.Python);

        final RobotCommandsExecutors executors = mock(RobotCommandsExecutors.class);
        when(executors.getRobotCommandExecutor(location)).thenReturn(executor);

        final RobotRuntimeEnvironment env = new RobotRuntimeEnvironment(executors, location, "3.0.0");

        final List<File> modulesSearchPaths = env.getModuleSearchPaths();

        assertThat(modulesSearchPaths.stream().map(File::getName)).containsOnly("MoDuLe", "module2");
    }

    @Test
    public void emptyPossibleInstallationDirectoriesAreReturned_whenLocationDoesNotExist() throws Exception {
        assertThat(RobotRuntimeEnvironment.possibleInstallationsFor(new File("not_existing_location"))).isEmpty();
    }

    @Test
    public void emptyPossibleInstallationDirectoriesAreReturned_whenLocationDoesNotContainExecutable()
            throws Exception {
        final File location = folder.newFolder("no_executable");
        folder.newFile("no_executable/notExecutor");
        folder.newFile("no_executable/nested_dir");

        assertThat(RobotRuntimeEnvironment.possibleInstallationsFor(location)).isEmpty();
    }

    @Test
    public void possibleInstallationDirectoriesAreReturned_forEveryExecutor() throws Exception {
        for (final SuiteExecutor executor : EnumSet.allOf(SuiteExecutor.class)) {
            final File location = folder.newFolder(executor.name() + "_executable");
            folder.newFile(location.getName() + "/" + executor.executableName());
            folder.newFile(location.getName() + "/notExecutor");
            folder.newFile(location.getName() + "/nested_dir");

            assertThat(RobotRuntimeEnvironment.possibleInstallationsFor(location))
                    .containsOnly(new PythonInstallationDirectory(location.toURI(), executor));
        }
    }

    @Test
    public void allPossibleInstallationDirectoriesAreReturned_whenSeveralExecutablesExist() throws Exception {
        final File location = folder.newFolder("several_executables");
        folder.newFile("several_executables/" + SuiteExecutor.Python.executableName());
        folder.newFile("several_executables/" + SuiteExecutor.Python2.executableName());
        folder.newFile("several_executables/" + SuiteExecutor.Python3.executableName());
        folder.newFile("several_executables/notExecutor");
        folder.newFile("several_executables/nested_dir");

        assertThat(RobotRuntimeEnvironment.possibleInstallationsFor(location)).containsOnly(
                new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Python),
                new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Python2),
                new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Python3));
    }
}
