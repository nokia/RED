/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;

/**
 * @author mmarzec
 */
public class RobotDryRunTemporarySuites {

    public static Optional<File> createResourceImportFile(final Collection<String> resourcesPaths) {
        return resourcesPaths.isEmpty() ? Optional.empty() : createFile(resourcesPaths, Collections.emptyList());
    }

    public static Optional<File> createLibraryImportFile(final Collection<String> libraryNames) {
        return libraryNames.isEmpty() ? Optional.empty() : createFile(Collections.emptyList(), libraryNames);
    }

    private static Optional<File> createFile(final Collection<String> resourcesPaths,
            final Collection<String> libraryNames) {
        try {
            final File file = RobotRuntimeEnvironment.createTemporaryFile("DryRunTempSuite.robot");
            try (PrintWriter printWriter = new PrintWriter(file)) {
                printWriter.println("*** Test Cases ***");
                printWriter.println("T1");
                printWriter.println("*** Settings ***");
                for (final String path : resourcesPaths) {
                    printWriter.println("Resource  " + path);
                }
                for (final String name : libraryNames) {
                    printWriter.println("Library  " + name);
                }
            }
            return Optional.of(file);
        } catch (final IOException e) {
            return Optional.empty();
        }
    }
}
