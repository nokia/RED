/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.libraries.SitePackagesLibraries;
import org.rf.ide.core.rflint.RfLintRule;

import com.google.common.annotations.VisibleForTesting;

@SuppressWarnings({ "PMD.GodClass", "PMD.TooManyMethods" })
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

    public static String getVersion(final SuiteExecutor interpreter) throws RobotEnvironmentException {
        final Optional<PythonInstallationDirectory> installationDirectory = PythonInstallationDirectoryFinder
                .whereIsPythonInterpreter(interpreter);
        final PythonInstallationDirectory location = installationDirectory
                .orElseThrow(() -> new RobotEnvironmentException(
                        "There is no " + interpreter.name() + " interpreter in system PATH environment variable"));
        return getExactVersion(location);
    }

    private static String getExactVersion(final PythonInstallationDirectory location) {
        final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                .getRobotCommandExecutor(location);
        final String version = executor.getRobotVersion();
        return location.getInterpreter().exactVersion(version);
    }

    public static RobotRuntimeEnvironment create(final String pathToPython) {
        return create(new File(pathToPython));
    }

    public static RobotRuntimeEnvironment create(final File pathToPython) {
        try {
            final PythonInstallationDirectory location = PythonInstallationDirectoryFinder
                    .checkPythonInstallationDir(pathToPython);
            return new RobotRuntimeEnvironment(location, getExactVersion(location));
        } catch (final IllegalArgumentException e) {
            return new RobotRuntimeEnvironment(pathToPython, null);
        }
    }

    public static RobotRuntimeEnvironment create(final String pathToPython, final SuiteExecutor interpreter) {
        return create(new File(pathToPython), interpreter);
    }

    public static RobotRuntimeEnvironment create(final File pathToPython, final SuiteExecutor interpreter) {
        try {
            final PythonInstallationDirectory location = PythonInstallationDirectoryFinder
                    .checkPythonInstallationDir(pathToPython);
            final PythonInstallationDirectory correctedLocation = new PythonInstallationDirectory(location.toURI(),
                    interpreter);
            return new RobotRuntimeEnvironment(correctedLocation, getExactVersion(correctedLocation));
        } catch (final IllegalArgumentException e) {
            return new RobotRuntimeEnvironment(pathToPython, null);
        }
    }

    private RobotRuntimeEnvironment(final File location, final String version) {
        this(PythonInterpretersCommandExecutors.getInstance(), location, version);
    }

    @VisibleForTesting
    public RobotRuntimeEnvironment(final RobotCommandsExecutors executors, final File location, final String version) {
        this.executors = executors;
        this.location = location;
        this.version = version;
    }

    public boolean isValidPythonInstallation() {
        return location instanceof PythonInstallationDirectory;
    }

    public boolean hasRobotInstalled() {
        return isValidPythonInstallation() && version != null;
    }

    public boolean isCompatibleRobotInstallation() {
        return hasRobotInstalled() && !PythonVersion.from(version).isDeprecated();
    }

    public String getVersion() {
        return version;
    }

    public SuiteExecutor getInterpreter() {
        return location instanceof PythonInstallationDirectory
                ? ((PythonInstallationDirectory) location).getInterpreter()
                : null;
    }

    public File getFile() {
        return location;
    }

    public String getPythonExecutablePath() {
        final PythonInstallationDirectory pyLocation = (PythonInstallationDirectory) location;
        final String pythonExec = pyLocation.getInterpreter().executableName();
        return Stream.of(pyLocation.listFiles())
                .filter(file -> pythonExec.equals(file.getName()))
                .findFirst()
                .map(File::getAbsolutePath)
                .orElse(pythonExec);
    }

    public void resetCommandExecutors() {
        if (hasRobotInstalled()) {
            executors.resetExecutorFor((PythonInstallationDirectory) location);
        }
    }

    public List<File> getModuleSearchPaths() {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getModulesSearchPaths().stream().map(this::tryToCanonical).collect(toList());
        }
        return new ArrayList<>();
    }

    public Optional<File> getModulePath(final String moduleName, final EnvironmentSearchPaths additionalPaths) {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return Optional.of(executor.getModulePath(moduleName, additionalPaths)).map(this::tryToCanonical);
        }
        return Optional.empty();
    }

    private File tryToCanonical(final File file) {
        try {
            return file.getCanonicalFile();
        } catch (final IOException e) {
            return file;
        }
    }

    public void createLibdoc(final String libName, final File outputFile, final LibdocFormat format)
            throws RobotEnvironmentException {
        createLibdoc(libName, outputFile, format, new EnvironmentSearchPaths());
    }

    public void createLibdoc(final String libName, final File outputFile, final LibdocFormat format,
            final EnvironmentSearchPaths additionalPaths) throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.createLibdoc(libName, outputFile, format, additionalPaths);
        }
    }

    public String createHtmlDoc(final String doc, final DocFormat format) {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.createHtmlDoc(doc, format);
        }
        return "";
    }

    public List<String> getStandardLibrariesNames() {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            final List<String> libs = executor.getStandardLibrariesNames();
            // Remote is a library without keywords and libdoc throws
            // exceptions when trying to generate its specification
            libs.remove("Remote");
            return libs;
        } else {
            return new ArrayList<>();
        }
    }

    public Optional<File> getStandardLibraryPath(final String libraryName) {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return Optional.of(executor.getStandardLibraryPath(libraryName));
        }
        return Optional.empty();
    }

    public SitePackagesLibraries getSitePackagesLibrariesNames() {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            final List<List<String>> libs = executor.getSitePackagesLibrariesNames();
            return new SitePackagesLibraries(libs);
        }
        return new SitePackagesLibraries();
    }

    /**
     * Return names of python classes contained in module point by argument and
     * all of its submodules. For packages-module __init__.py file path should
     * be provided.
     *
     * @param moduleLocation
     *            Module location
     * @return list of class names or empty list
     * @throws RobotEnvironmentException
     */
    public List<String> getClassesFromModule(final File moduleLocation, final EnvironmentSearchPaths additionalPaths)
            throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getClassesFromModule(moduleLocation, additionalPaths);
        }
        return new ArrayList<>();
    }

    public Map<String, Object> getGlobalVariables() {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getGlobalVariables();
        }
        return new LinkedHashMap<>();
    }

    public Map<String, Object> getVariablesFromFile(final String path, final List<String> args) {
        if (hasRobotInstalled()) {
            final String normalizedPath = path.replace('\\', '/');
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getVariables(normalizedPath, args);
        }
        return new LinkedHashMap<>();
    }

    public void runRfLint(final String host, final int port, final File projectLocation,
            final List<String> excludedPaths, final File filepath, final List<RfLintRule> rules,
            final List<String> rulesFiles, final List<String> additionalArguments) {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.runRfLint(host, port, projectLocation, excludedPaths, filepath, rules, rulesFiles,
                    additionalArguments);
        }
    }

    public String convertRobotDataFile(final File originalFile) {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.convertRobotDataFile(originalFile);
        }
        return "";
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
     * @throws RobotEnvironmentException
     */
    public void startLibraryAutoDiscovering(final int port, final File dataSource, final File projectLocation,
            final boolean recursiveInVirtualenv, final List<String> excludedPaths,
            final EnvironmentSearchPaths additionalPaths) throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.startLibraryAutoDiscovering(port, dataSource, projectLocation, recursiveInVirtualenv,
                    excludedPaths, additionalPaths);
        }
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
     * @throws RobotEnvironmentException
     */
    public void startKeywordAutoDiscovering(final int port, final File dataSource,
            final EnvironmentSearchPaths additionalPaths) throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.startKeywordAutoDiscovering(port, dataSource, additionalPaths);
        }
    }

    public void stopAutoDiscovering() throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.stopAutoDiscovering();
        }
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
