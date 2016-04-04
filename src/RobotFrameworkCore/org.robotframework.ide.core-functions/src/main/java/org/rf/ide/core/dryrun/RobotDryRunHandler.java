/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.rf.ide.core.executor.ILineHandler;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder;
import org.rf.ide.core.executor.TestRunnerAgentHandler;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.IRunCommandLineBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;

/**
 * @author mmarzec
 */
public class RobotDryRunHandler {
    
    private Process dryRunProcess;

    public RunCommandLine buildDryRunCommand(final RobotRuntimeEnvironment environment, final File projectLocation,
            final Collection<String> suites, final Collection<String> pythonPathLocations,
            final Collection<String> classPathLocations, final Collection<String> additionalProjectsLocations)
                    throws IOException {

        final IRunCommandLineBuilder builder = RunCommandLineCallBuilder.forEnvironment(environment);

        builder.withProject(projectLocation);
        builder.suitesToRun(suites);
        builder.addLocationsToPythonPath(pythonPathLocations);
        builder.addLocationsToClassPath(classPathLocations);
        builder.enableDebug(false);
        builder.enableDryRun(true);
        builder.withAdditionalProjectsLocations(additionalProjectsLocations);

        return builder.build();
    }

    public void startDryRunHandlerThread(final int port, final List<ILineHandler> listeners) {
        final TestRunnerAgentHandler testRunnerAgentHandler = new TestRunnerAgentHandler(port);
        for (final ILineHandler listener : listeners) {
            testRunnerAgentHandler.addListener(listener);
        }
        final Thread handlerThread = new Thread(testRunnerAgentHandler);
        handlerThread.start();
    }

    public void executeDryRunProcess(final RunCommandLine dryRunCommandLine) throws InvocationTargetException {
        if (dryRunCommandLine != null) {
            try {
                dryRunProcess = new ProcessBuilder(dryRunCommandLine.getCommandLine()).start();
                drainProcessOutputAndErrorStreams(dryRunProcess);
                if (dryRunProcess != null) {
                    dryRunProcess.waitFor();
                }
            } catch (InterruptedException | IOException e) {
                throw new InvocationTargetException(e);
            }
        }
    }
    
    public void destroyDryRunProcess() {
        if (dryRunProcess != null) {
            dryRunProcess.destroy();
        }
    }

    public File createTempSuiteFile(final List<String> resourcesPaths) {
        File file = null;
        PrintWriter printWriter = null;
        try {
            file = RobotRuntimeEnvironment.copyResourceFile("DryRunTempSuite.robot");
            printWriter = new PrintWriter(file);
            printWriter.println("*** Test Cases ***");
            printWriter.println("T1");
            printWriter.println("*** Settings ***");
            for (final String path : resourcesPaths) {
                printWriter.println("Resource  " + path);
            }
        } catch (IOException e) {
            // nothing to do
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
        return file;
    }

    private void drainProcessOutputAndErrorStreams(final Process process) {
        startStdOutReadingThread(process);
        startStdErrReadingThread(process);
    }

    private void startStdErrReadingThread(final Process process) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                final InputStream inputStream = process.getErrorStream();
                try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line = reader.readLine();
                    while (line != null) {
                        line = reader.readLine();
                    }
                } catch (final IOException e) {
                    // nothing to do
                }
            }
        }).start();
    }

    private void startStdOutReadingThread(final Process process) {
        new Thread(new Runnable() {

            @Override
            public void run() {

                final InputStream inputStream = process.getInputStream();
                try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line = reader.readLine();
                    while (line != null) {
                        line = reader.readLine();
                    }
                } catch (final IOException e) {
                    // nothing to do
                }
            }
        }).start();
    }
}
