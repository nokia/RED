/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Test;

public class RobotDryRunTemporarySuitesTest {

    @Test
    public void libraryImportFileIsCreated() throws Exception {
        final Optional<File> file = RobotDryRunTemporarySuites
                .createLibraryImportFile(Arrays.asList("lib_1", "lib_2", "lib_3"));
        assertThat(file).hasValueSatisfying(sameLinesRequirement("*** Test Cases ***", "T1", "*** Settings ***",
                "Library  lib_1", "Library  lib_2", "Library  lib_3"));
    }

    @Test
    public void libraryImportFileIsNotCreated_whenLibraryNamesAreEmpty() throws Exception {
        final Optional<File> file = RobotDryRunTemporarySuites.createLibraryImportFile(Collections.emptyList());
        assertThat(file).isNotPresent();
    }

    private Consumer<File> sameLinesRequirement(final String... lines) {
        return file -> {
            assertThat(file).hasContent(String.join(System.lineSeparator(), lines));
        };
    }
}
