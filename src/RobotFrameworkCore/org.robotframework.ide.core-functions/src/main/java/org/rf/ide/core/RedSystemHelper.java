/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Charsets;

public final class RedSystemHelper {

    /**
     * Run external process with error stream redirection and wait until it is terminated.
     *
     * @param command
     *            System command to run
     * @param lineHandler
     *            Output lines handler
     * @return The exit value of the subprocess which was run
     * @throws IOException
     *             thrown if the current thread is interrupted by another thread while it is
     *             waiting.
     */
    public static int runExternalProcess(final List<String> command, final Consumer<String> lineHandler)
            throws IOException {
        return runExternalProcess(null, command, lineHandler);
    }

    /**
     * Run external process in specified working directory with error stream redirection and wait
     * until
     * it is terminated.
     *
     * @param workingDirectory
     *            Working directory for external process
     * @param command
     *            System command to run
     * @param lineHandler
     *            Output lines handler
     * @return The exit value of the subprocess which was run
     * @throws IOException
     *             thrown if the current thread is interrupted by another thread while it is
     *             waiting.
     */
    public static int runExternalProcess(final File workingDirectory, final List<String> command,
            final Consumer<String> lineHandler) throws IOException {
        try {
            final ProcessBuilder builder = new ProcessBuilder(command);
            if (workingDirectory != null) {
                builder.directory(workingDirectory);
            }
            final Process process = builder.redirectErrorStream(true).start();

            final InputStream inputStream = process.getInputStream();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineHandler.accept(line);
                }
            }
            return process.waitFor();
        } catch (final InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     * Locates paths in which executable pointed in environment path is located.
     * Uses 'where' command in Windows and 'which' command under Unix.
     *
     * @param executableName
     *            Executable to find
     * @return Output lines of system where/which command or empty list.
     */
    public static List<String> findExecutablePaths(final String executableName) {
        final List<String> paths = new ArrayList<>();
        try {
            final String cmd = RedSystemProperties.isWindowsPlatform() ? "where" : "which";
            final int exitCode = runExternalProcess(Arrays.asList(cmd, executableName), line -> paths.add(line));
            return exitCode == 0 ? paths : new ArrayList<>();
        } catch (final IOException e) {
            return new ArrayList<>();
        }
    }

}
