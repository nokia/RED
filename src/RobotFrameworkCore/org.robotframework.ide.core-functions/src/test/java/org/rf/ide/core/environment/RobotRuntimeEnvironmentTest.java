/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.environment;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rf.ide.core.RedSystemProperties;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;

public class RobotRuntimeEnvironmentTest {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void beforeSuite() throws IOException {
        folder.newFile("MoDuLe");
    }

    @Test
    public void testRobotInstallationCompatibility() throws Exception {
        assertThat(new RobotRuntimeEnvironment(null, null, "RF 3.0.2 (Python 2.6.9 on win32)")
                .isCompatibleRobotInstallation()).isFalse();
        assertThat(new RobotRuntimeEnvironment(null, null, "RF 3.0.2 (Python 2.7.1 on win32)")
                .isCompatibleRobotInstallation()).isTrue();
    }

    @Test
    public void moduleCanonicalPathIsReturned_evenWhenExecutorReturnsLowerCasePath() {
        // this test only makes sense on case-insensitive platforms like windows
        assumeTrue(RedSystemProperties.isWindowsPlatform());

        final EnvironmentSearchPaths searchPaths = new EnvironmentSearchPaths();

        final RobotCommandExecutor executor = mock(RobotCommandExecutor.class);
        when(executor.getModulePath("module", searchPaths)).thenReturn(new File(folder.getRoot(), "module"));

        final RobotRuntimeEnvironment env = createEnvironment(executor);

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

        final RobotRuntimeEnvironment env = createEnvironment(executor);

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

        final RobotRuntimeEnvironment env = createEnvironment(executor);

        final List<File> modulesSearchPaths = env.getModuleSearchPaths();

        assertThat(modulesSearchPaths).extracting(File::getName).containsOnly("MoDuLe", "module2");
    }

    @Test
    public void remoteLibraryIsFilteredOut_whenStandardLibrariesNamesAreReturned() {
        final RobotCommandExecutor executor = mock(RobotCommandExecutor.class);
        when(executor.getStandardLibrariesNames()).thenReturn(newArrayList("BuiltIn", "Dialogs", "Remote", "XML"));

        final RobotRuntimeEnvironment env = createEnvironment(executor);

        final List<String> stdLibNames = env.getStandardLibrariesNames();

        assertThat(stdLibNames).containsExactly("BuiltIn", "Dialogs", "XML");
    }

    private RobotRuntimeEnvironment createEnvironment(final RobotCommandExecutor executor) {
        final PythonInstallationDirectory location = new PythonInstallationDirectory(
                URI.create("file:///path/to/python"), SuiteExecutor.Python);

        final RobotCommandsExecutors executors = mock(RobotCommandsExecutors.class);
        when(executors.getRobotCommandExecutor(location)).thenReturn(executor);

        return new RobotRuntimeEnvironment(executors, location, "3.0.0");
    }

}
