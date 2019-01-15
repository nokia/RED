/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.libraries.SitePackagesLibraries;
import org.rf.ide.core.rflint.RfLintRule;

import com.google.common.annotations.VisibleForTesting;

public class RobotRuntimeEnvironment {

    private final RobotCommandsExecutors executors;

    private final File location;

    private final String version;

    public static void addProcessListener(final PythonProcessListener listener) {
        PythonInterpretersCommandExecutors.getInstance().addProcessListener(listener);
    }

    public static void removeProcessListener(final PythonProcessListener listener) {
        PythonInterpretersCommandExecutors.getInstance().removeProcessListener(listener);
    }

    public RobotRuntimeEnvironment(final File location, final String version) {
        this(PythonInterpretersCommandExecutors.getInstance(), location, version);
    }

    @VisibleForTesting
    RobotRuntimeEnvironment(final RobotCommandsExecutors executors, final File location, final String version) {
        this.executors = executors;
        this.location = location;
        this.version = version;
    }

    public boolean isNullEnvironment() {
        return false;
    }

    public boolean isValidPythonInstallation() {
        return true;
    }

    public boolean hasRobotInstalled() {
        return true;
    }

    public boolean isCompatibleRobotInstallation() {
        return !PythonVersion.from(version).isDeprecated();
    }

    public String getVersion() {
        return version;
    }

    public RobotVersion getRobotVersion() {
        return RobotVersion.from(version);
    }

    public SuiteExecutor getInterpreter() {
        return ((PythonInstallationDirectory) location).getInterpreter();
    }

    public File getFile() {
        return location;
    }

    public String getPythonExecutablePath() {
        final SuiteExecutor interpreter = getInterpreter();
        final String pythonExec = interpreter.executableName();
        return Stream.of(location.listFiles())
                .filter(file -> pythonExec.equals(file.getName()))
                .findFirst()
                .map(File::getAbsolutePath)
                .orElse(pythonExec);
    }

    public void resetCommandExecutors() {
        executors.resetExecutorFor((PythonInstallationDirectory) location);
    }

    public List<File> getModuleSearchPaths() {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        return executor.getModulesSearchPaths().stream().map(this::tryToCanonical).collect(toList());
    }

    public Optional<File> getModulePath(final String moduleName, final EnvironmentSearchPaths additionalPaths) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        return Optional.of(executor.getModulePath(moduleName, additionalPaths)).map(this::tryToCanonical);
    }

    private File tryToCanonical(final File file) {
        try {
            return file.getCanonicalFile();
        } catch (final IOException e) {
            return file;
        }
    }

    public void createLibdoc(final String libName, final File outputFile, final LibdocFormat format) {
        createLibdoc(libName, outputFile, format, new EnvironmentSearchPaths());
    }

    public void createLibdoc(final String libName, final File outputFile, final LibdocFormat format,
            final EnvironmentSearchPaths additionalPaths) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        executor.createLibdoc(libName, outputFile, format, additionalPaths);
    }

    public void createLibdocInSeparateProcess(final String libName, final File outputFile, final LibdocFormat format) {
        createLibdocInSeparateProcess(libName, outputFile, format, new EnvironmentSearchPaths());
    }

    public void createLibdocInSeparateProcess(final String libName, final File outputFile, final LibdocFormat format,
            final EnvironmentSearchPaths additionalPaths) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        executor.createLibdocInSeparateProcess(libName, outputFile, format, additionalPaths);
    }

    public String createHtmlDoc(final String doc, final DocFormat format) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        return executor.createHtmlDoc(doc, format);
    }

    public List<String> getStandardLibrariesNames() {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        final List<String> libs = executor.getStandardLibrariesNames();
        // Remote is a library without keywords and libdoc throws
        // exceptions when trying to generate its specification
        libs.remove("Remote");
        return libs;
    }

    public Optional<File> getStandardLibraryPath(final String libraryName) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        return Optional.of(executor.getStandardLibraryPath(libraryName));
    }

    public SitePackagesLibraries getSitePackagesLibrariesNames() {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        final List<List<String>> libs = executor.getSitePackagesLibrariesNames();
        return new SitePackagesLibraries(libs.get(0), libs.get(1));
    }

    /**
     * Return names of python classes contained in module point by argument and
     * all of its submodules. For packages-module __init__.py file path should
     * be provided.
     *
     * @param moduleLocation
     *            Module location
     * @return list of class names or empty list
     */
    public List<String> getClassesFromModule(final File moduleLocation, final EnvironmentSearchPaths additionalPaths) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        return executor.getClassesFromModule(moduleLocation, additionalPaths);
    }

    public Map<String, Object> getGlobalVariables() {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        return executor.getGlobalVariables();
    }

    public Map<String, Object> getVariablesFromFile(final File source, final List<String> arguments) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        return executor.getVariables(source, arguments);
    }

    public void runRfLint(final String host, final int port, final File projectLocation,
            final List<String> excludedPaths, final File filepath, final List<RfLintRule> rules,
            final List<String> rulesFiles, final List<String> additionalArguments) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        executor.runRfLint(host, port, projectLocation, excludedPaths, filepath, rules, rulesFiles,
                additionalArguments);
    }

    public String convertRobotDataFile(final File originalFile) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        return executor.convertRobotDataFile(originalFile);
    }

    /**
     * Start library auto discovering with robot dryrun
     *
     * @param port
     *            Port number for communication with AgentConnectionServer
     * @param dataSource
     *            Test case file with unknown library imports
     * @param projectLocation
     *            Project file
     * @param recursiveInVirtualenv
     *            Virtualenv recursive library source lookup switch
     * @param excludedPaths
     *            Project relative excluded paths
     * @param additionalPaths
     *            Additional pythonPaths and classPaths
     */
    public void startLibraryAutoDiscovering(final int port, final File dataSource, final File projectLocation,
            final boolean recursiveInVirtualenv, final List<String> excludedPaths,
            final EnvironmentSearchPaths additionalPaths) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        executor.startLibraryAutoDiscovering(port, dataSource, projectLocation, recursiveInVirtualenv, excludedPaths,
                additionalPaths);
    }

    /**
     * Start keyword auto discovering with robot dryrun
     *
     * @param port
     *            Port number for communication with AgentConnectionServer
     * @param dataSource
     *            Test case file with known library imports
     * @param additionalPaths
     *            Additional pythonPaths and classPaths
     */
    public void startKeywordAutoDiscovering(final int port, final File dataSource,
            final EnvironmentSearchPaths additionalPaths) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        executor.startKeywordAutoDiscovering(port, dataSource, additionalPaths);
    }

    public void stopAutoDiscovering() {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor((PythonInstallationDirectory) location);
        executor.stopAutoDiscovering();
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, version);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RobotRuntimeEnvironment other = (RobotRuntimeEnvironment) obj;
        return Objects.equals(location, other.location) && Objects.equals(version, other.version);
    }

    @SuppressWarnings("serial")
    public static class RobotEnvironmentException extends RuntimeException {

        public RobotEnvironmentException(final String message) {
            super(message);
        }

        public RobotEnvironmentException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    public static enum LibdocFormat {
        XML,
        HTML
    }
}
