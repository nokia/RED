/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;

/**
 * @author mmarzec
 */
public class RobotDryRunHandler {

    private Process dryRunProcess;

    public void executeDryRunProcess(final RunCommandLine dryRunCommandLine, final File projectDir)
            throws InvocationTargetException {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(dryRunCommandLine.getCommandLine());
            if (projectDir != null && projectDir.exists()) {
                processBuilder = processBuilder.directory(projectDir);
            }
            dryRunProcess = processBuilder.start();
            dryRunProcess.waitFor();
        } catch (InterruptedException | IOException e) {
            throw new InvocationTargetException(e);
        }
    }

    public void destroyDryRunProcess() {
        if (dryRunProcess != null) {
            dryRunProcess.destroy();
        }
    }

    public File createTempSuiteFile(final List<String> resourcesPaths, final List<String> libraryNames) {
        File file = null;
        PrintWriter printWriter = null;
        try {
            file = RobotRuntimeEnvironment.createTemporaryFile("DryRunTempSuite.robot");
            printWriter = new PrintWriter(file);
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
