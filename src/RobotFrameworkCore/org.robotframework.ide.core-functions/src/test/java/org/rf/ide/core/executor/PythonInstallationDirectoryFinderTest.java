/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.EnumSet;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rf.ide.core.executor.PythonInstallationDirectoryFinder.PythonInstallationDirectory;

public class PythonInstallationDirectoryFinderTest {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void emptyPossibleInstallationDirectoriesAreReturned_whenLocationDoesNotExist() throws Exception {
        assertThat(PythonInstallationDirectoryFinder.findPossibleInstallationsFor(new File("not_existing_location")))
                .isEmpty();
    }

    @Test
    public void emptyPossibleInstallationDirectoriesAreReturned_whenLocationDoesNotContainExecutable()
            throws Exception {
        final File location = folder.newFolder("no_executable");
        folder.newFile("no_executable/notExecutor");
        folder.newFile("no_executable/nested_dir");

        assertThat(PythonInstallationDirectoryFinder.findPossibleInstallationsFor(location)).isEmpty();
    }

    @Test
    public void possibleInstallationDirectoriesAreReturned_forEveryExecutor() throws Exception {
        for (final SuiteExecutor executor : EnumSet.allOf(SuiteExecutor.class)) {
            final File location = folder.newFolder(executor.name() + "_executable");
            folder.newFile(location.getName() + "/" + executor.executableName());
            folder.newFile(location.getName() + "/notExecutor");
            folder.newFile(location.getName() + "/nested_dir");

            assertThat(PythonInstallationDirectoryFinder.findPossibleInstallationsFor(location))
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

        assertThat(PythonInstallationDirectoryFinder.findPossibleInstallationsFor(location)).containsOnly(
                new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Python),
                new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Python2),
                new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Python3));
    }

}
