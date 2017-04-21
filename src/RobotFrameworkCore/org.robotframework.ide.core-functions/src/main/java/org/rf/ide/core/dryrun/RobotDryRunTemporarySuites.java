/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;

/**
 * @author mmarzec
 */
public class RobotDryRunTemporarySuites {

    public static File createResourceFile(final List<String> resourcesPaths) {
        return createFile(resourcesPaths, new ArrayList<String>());
    }

    public static File createLibraryFile(final List<String> libraryNames) {
        return createFile(new ArrayList<String>(), libraryNames);
    }

    private static File createFile(final List<String> resourcesPaths, final List<String> libraryNames) {
        File file = null;
        PrintWriter printWriter = null;
        try {
            file = RobotRuntimeEnvironment.createTemporaryFile("DryRunTempSuite.robot");
            printWriter = new PrintWriter(file);
            printWriter.println("*** Test Cases ***");
            printWriter.println("T1");
            printWriter.println("*** Settings ***");
            for (final String path : resourcesPaths) {
                printWriter.println("Resource  " + path);
            }
            for (final String name : libraryNames) {
                printWriter.println("Library  " + name);
            }
        } catch (final IOException e) {
            // nothing to do
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
        return file;
    }
}
