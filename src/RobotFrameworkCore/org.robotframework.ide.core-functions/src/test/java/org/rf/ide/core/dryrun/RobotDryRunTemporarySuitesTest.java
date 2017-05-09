/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.common.io.Files;

public class RobotDryRunTemporarySuitesTest {

    @Test
    public void resourceImportFileIsCreated() throws Exception {
        final File file = RobotDryRunTemporarySuites.createResourceFile(Arrays.asList("res_1", "res_2", "res_3"));
        final List<String> lines = Files.readLines(file, StandardCharsets.UTF_8);
        assertThat(lines).containsExactly("*** Test Cases ***", "T1", "*** Settings ***", "Resource  res_1",
                "Resource  res_2", "Resource  res_3");
    }

    @Test
    public void libraryImportFileIsCreated() throws Exception {
        final File file = RobotDryRunTemporarySuites.createLibraryFile(Arrays.asList("lib_1", "lib_2", "lib_3"));
        final List<String> lines = Files.readLines(file, StandardCharsets.UTF_8);
        assertThat(lines).containsExactly("*** Test Cases ***", "T1", "*** Settings ***", "Library  lib_1",
                "Library  lib_2", "Library  lib_3");
    }
}
