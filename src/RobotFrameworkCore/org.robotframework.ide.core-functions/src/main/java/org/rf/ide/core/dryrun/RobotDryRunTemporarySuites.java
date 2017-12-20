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
import java.util.Optional;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;

/**
 * @author mmarzec
 */
public class RobotDryRunTemporarySuites {

    public static Optional<File> createLibraryImportFile(final Collection<String> libraryNames) {
        if (libraryNames.isEmpty()) {
            return Optional.empty();
        }

        try {
            final File file = RobotRuntimeEnvironment.createTemporaryFile("DryRunTempSuite.robot");
            try (PrintWriter printWriter = new PrintWriter(file, "UTF-8")) {
                printWriter.println("*** Test Cases ***");
                printWriter.println("T1");
                printWriter.println("*** Settings ***");
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
