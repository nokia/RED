/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.rf.ide.core.executor.RunCommandLineCallBuilder.IRunCommandLineBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;

/**
 * @author mmarzec
 */
public class RobotDryRunHandler {

    public RunCommandLine buildDryRunCommand(final RobotRuntimeEnvironment environment, final File projectLocation,
            final Collection<String> suites, final Collection<String> pythonPathLocations,
            final Collection<String> classPathLocations) throws IOException {

        final IRunCommandLineBuilder builder = RunCommandLineCallBuilder.forEnvironment(environment);

        builder.withProject(projectLocation);
        builder.suitesToRun(suites);
        builder.addLocationsToPythonPath(pythonPathLocations);
        builder.addLocationsToClassPath(classPathLocations);
        builder.enableDebug(false);

        builder.addUserArgumentsForRobot("--dryrun");

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
                final Process process = new ProcessBuilder(dryRunCommandLine.getCommandLine()).start();
                drainProcessOutputAndErrorStreams(process);
                if (process != null) {
                    process.waitFor();
                }
            } catch (InterruptedException | IOException e) {
                throw new InvocationTargetException(e);
            }
        }
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
