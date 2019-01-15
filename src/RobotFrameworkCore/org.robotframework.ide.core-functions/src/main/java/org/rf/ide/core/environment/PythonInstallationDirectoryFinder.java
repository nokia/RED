/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

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
        if (!location.exists() || !location.isDirectory()) {
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

    public static Optional<PythonInstallationDirectory> findInstallation(final File location,
            final SuiteExecutor interpreter) {
        if (interpreter == null) {
            return findPossibleInstallationsFor(location).stream().findFirst();
        }
        return findPossibleInstallationsFor(location).stream()
                .findFirst()
                .map(dir -> new PythonInstallationDirectory(dir.toURI(), interpreter));
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

        public Optional<String> getRobotVersion() {
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getRobotCommandExecutor(this);
            return Optional.ofNullable(executor.getRobotVersion()).map(interpreter::exactVersion);
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
