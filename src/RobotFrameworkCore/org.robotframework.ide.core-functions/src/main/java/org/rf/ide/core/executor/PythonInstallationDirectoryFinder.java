/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.rf.ide.core.RedSystemHelper;

public final class PythonInstallationDirectoryFinder {

    /**
     * Locates directories in which python interpreters pointed in environment path are located.
     * Uses 'where' command in Windows and 'which' command under Unix.
     *
     * @return Directories where python interpreters are installed or empty list
     *         if there is no python at all.
     */
    public static List<PythonInstallationDirectory> whereArePythonInterpreters() {
        return EnumSet.allOf(SuiteExecutor.class)
                .stream()
                .map(PythonInstallationDirectoryFinder::whereIsPythonInterpreter)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    /**
     * Locates directory in which python interpreter pointed in environment path is located.
     * Uses 'where' command in Windows and 'which' command under Unix.
     *
     * @param interpreter
     *            Interpreter to find
     * @return Directory where python interpreter is installed or empty if not found
     */
    public static Optional<PythonInstallationDirectory> whereIsPythonInterpreter(final SuiteExecutor interpreter) {
        final List<String> paths = RedSystemHelper.findExecutablePaths(interpreter.executableName());
        return paths.stream()
                .map(path -> new File(path).getParentFile().toURI())
                .map(dirUri -> new PythonInstallationDirectory(dirUri, interpreter))
                .findFirst();
    }

    /**
     * Locates directories in which python interpreters pointed by location are located.
     *
     * @param location
     *            Directory where python interpreters should be found
     * @return Directories where python interpreters are installed or empty list if not found.
     */
    public static List<PythonInstallationDirectory> findPossibleInstallationsFor(final File location) {
        if (!location.exists()) {
            return new ArrayList<>();
        }
        return Stream.of(location.listFiles())
                .filter(File::isFile)
                .flatMap(file -> EnumSet.allOf(SuiteExecutor.class)
                        .stream()
                        .filter(executor -> file.getName().equals(executor.executableName())))
                .map(interpreter -> new PythonInstallationDirectory(location.toURI(), interpreter))
                .collect(toList());
    }

    /**
     * Checks if given location is a directory which contains python interpreter.
     * The {@link IllegalArgumentException} exception is thrown if given location does not contain
     * python executable. Otherwise {@link PythonInstallationDirectory} instance (copy of location,
     * but with other type) is returned.
     *
     * @param location
     *            Location to check
     * @return The same location given as {@link File} subtype
     * @throws IllegalArgumentException
     *             thrown when given location is not a directory or does not contain python
     *             interpreter executables.
     */
    public static PythonInstallationDirectory checkPythonInstallationDir(final File location)
            throws IllegalArgumentException {
        if (!location.isDirectory()) {
            throw new IllegalArgumentException("The location " + location.getAbsolutePath() + " is not a directory.");
        }
        final List<PythonInstallationDirectory> installations = findPossibleInstallationsFor(location);
        if (installations.isEmpty()) {
            throw new IllegalArgumentException("The location: " + location.getAbsolutePath()
                    + " does not seem to be a valid python installation directory");
        }
        return installations.get(0);
    }

    @SuppressWarnings("serial")
    public static class PythonInstallationDirectory extends File {

        private final SuiteExecutor interpreter;

        // we don't want anyone to create those objects; they should only be created when given uri
        // is valid python location
        PythonInstallationDirectory(final URI uri, final SuiteExecutor interpreter) {
            super(uri);
            this.interpreter = interpreter;
        }

        public SuiteExecutor getInterpreter() {
            return interpreter;
        }

        public String getInterpreterPath() {
            return toPath().resolve(interpreter.executableName()).toAbsolutePath().toString();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof PythonInstallationDirectory) {
                final PythonInstallationDirectory that = (PythonInstallationDirectory) obj;
                return super.equals(obj) && this.interpreter == that.interpreter;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + ((interpreter == null) ? 0 : interpreter.hashCode());
        }
    }

}
