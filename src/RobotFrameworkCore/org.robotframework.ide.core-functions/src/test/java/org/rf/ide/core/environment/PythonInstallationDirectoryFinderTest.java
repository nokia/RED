/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.EnumSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;

public class PythonInstallationDirectoryFinderTest {

    @TempDir
    static File tempDir;

    @Test
    public void emptyPossibleInstallationDirectoriesAreReturned_whenLocationDoesNotExist() throws Exception {
        assertThat(PythonInstallationDirectoryFinder.findPossibleInstallationsFor("not_existing_location")).isEmpty();
    }

    @Test
    public void emptyPossibleInstallationDirectoriesAreReturned_whenLocationDoesNotContainExecutable()
            throws Exception {
        final File location = new File(tempDir, "no_executable");
        location.mkdir();
        new File(location, "notExecutor").createNewFile();
        new File(location, "nested_dir").mkdir();

        assertThat(PythonInstallationDirectoryFinder.findPossibleInstallationsFor(location.getPath())).isEmpty();
    }

    @Test
    public void possibleInstallationDirectoriesAreReturned_forEveryExecutor() throws Exception {
        for (final SuiteExecutor executor : EnumSet.allOf(SuiteExecutor.class)) {
            final File location = new File(tempDir, executor.name() + "_executable");
            location.mkdir();
            new File(location, executor.executableName()).createNewFile();
            new File(location, "notExecutor").createNewFile();
            new File(location, "nested_dir").mkdir();

            assertThat(PythonInstallationDirectoryFinder.findPossibleInstallationsFor(location.getPath()))
                    .containsOnly(new PythonInstallationDirectory(location.toURI(), executor));
        }
    }

    @Test
    public void allPossibleInstallationDirectoriesAreReturned_whenSeveralExecutablesExist() throws Exception {
        final File location = new File(tempDir, "several_executables");
        location.mkdir();
        new File(location, SuiteExecutor.Python.executableName()).createNewFile();
        new File(location, SuiteExecutor.Python2.executableName()).createNewFile();
        new File(location, SuiteExecutor.Python3.executableName()).createNewFile();
        new File(location, "notExecutor").createNewFile();
        new File(location, "nested_dir").mkdir();

        assertThat(PythonInstallationDirectoryFinder.findPossibleInstallationsFor(location.getPath())).containsOnly(
                new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Python),
                new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Python2),
                new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Python3));
    }

}
