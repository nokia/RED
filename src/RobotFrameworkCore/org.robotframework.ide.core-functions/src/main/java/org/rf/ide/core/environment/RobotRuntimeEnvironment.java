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

import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.libraries.LibrarySpecification.LibdocFormat;
import org.rf.ide.core.libraries.SitePackagesLibraries;
import org.rf.ide.core.rflint.RfLintRule;

import com.google.common.annotations.VisibleForTesting;

public class RobotRuntimeEnvironment implements IRuntimeEnvironment {

    private final RobotCommandsExecutors executors;

    private final PythonInstallationDirectory location;

    private final String version;

    public static void addProcessListener(final PythonProcessListener listener) {
        PythonInterpretersCommandExecutors.getInstance().addProcessListener(listener);
    }

    public static void removeProcessListener(final PythonProcessListener listener) {
        PythonInterpretersCommandExecutors.getInstance().removeProcessListener(listener);
    }

    public RobotRuntimeEnvironment(final PythonInstallationDirectory location, final String version) {
        this(PythonInterpretersCommandExecutors.getInstance(), location, version);
    }

    @VisibleForTesting
    RobotRuntimeEnvironment(final RobotCommandsExecutors executors, final PythonInstallationDirectory location,
            final String version) {
        this.executors = executors;
        this.location = location;
        this.version = version;
    }

    @Override
    public boolean isNullEnvironment() {
        return false;
    }

    @Override
    public boolean isValidPythonInstallation() {
        return true;
    }

    @Override
    public boolean hasRobotInstalled() {
        return true;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public RobotVersion getRobotVersion() {
        return RobotVersion.from(version);
    }

    @Override
    public SuiteExecutor getInterpreter() {
        return location.getInterpreter();
    }

    @Override
    public PythonInstallationDirectory getFile() {
        return location;
    }

    @Override
    public void resetCommandExecutors() {
        executors.resetExecutorFor(location);
    }

    @Override
    public List<File> getModuleSearchPaths() {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        return executor.getModulesSearchPaths().stream().map(this::tryToCanonical).collect(toList());
    }

    @Override
    public Optional<File> getModulePath(final String moduleName, final EnvironmentSearchPaths additionalPaths) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        return Optional.of(executor.getModulePath(moduleName, additionalPaths)).map(this::tryToCanonical);
    }

    private File tryToCanonical(final File file) {
        try {
            return file.getCanonicalFile();
        } catch (final IOException e) {
            return file;
        }
    }

    @Override
    public void createLibdoc(final String libName, final File outputFile, final LibdocFormat format,
            final EnvironmentSearchPaths additionalPaths) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        executor.createLibdoc(libName, outputFile, format, additionalPaths);
    }

    @Override
    public void createLibdocInSeparateProcess(final String libName, final File outputFile, final LibdocFormat format,
            final EnvironmentSearchPaths additionalPaths) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        executor.createLibdocInSeparateProcess(libName, outputFile, format, additionalPaths);
    }

    @Override
    public String createHtmlDoc(final String doc, final DocFormat format) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        return executor.createHtmlDoc(doc, format);
    }

    @Override
    public List<String> getStandardLibrariesNames() {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        final List<String> libs = executor.getStandardLibrariesNames();
        // Remote is a library without keywords and libdoc throws
        // exceptions when trying to generate its specification
        libs.remove("Remote");
        return libs;
    }

    @Override
    public Optional<File> getStandardLibraryPath(final String libraryName) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        return Optional.of(executor.getStandardLibraryPath(libraryName));
    }

    @Override
    public SitePackagesLibraries getSitePackagesLibrariesNames() {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        final List<List<String>> libs = executor.getSitePackagesLibrariesNames();
        return new SitePackagesLibraries(libs.get(0), libs.get(1));
    }

    @Override
    public List<String> getClassesFromModule(final File moduleLocation, final EnvironmentSearchPaths additionalPaths) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        return executor.getClassesFromModule(moduleLocation, additionalPaths);
    }

    @Override
    public Map<String, Object> getGlobalVariables() {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        return executor.getGlobalVariables();
    }

    @Override
    public Map<String, Object> getVariablesFromFile(final File source, final List<String> arguments) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        return executor.getVariables(source, arguments);
    }

    @Override
    public List<RfLintRule> getRfLintRules(final List<String> rulesFiles) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        return executor.getRfLintRules(rulesFiles);
    }

    @Override
    public void runRfLint(final String host, final int port, final File projectLocation,
            final List<String> excludedPaths, final File filepath, final List<RfLintRule> rules,
            final List<String> rulesFiles, final List<String> additionalArguments) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        executor.runRfLint(host, port, projectLocation, excludedPaths, filepath, rules, rulesFiles,
                additionalArguments);
    }

    @Override
    public String convertRobotDataFile(final File originalFile) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        return executor.convertRobotDataFile(originalFile);
    }

    @Override
    public void startLibraryAutoDiscovering(final int port, final File dataSource, final File projectLocation,
            final boolean recursiveInVirtualenv, final List<String> excludedPaths,
            final EnvironmentSearchPaths additionalPaths) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        executor.startLibraryAutoDiscovering(port, dataSource, projectLocation, recursiveInVirtualenv, excludedPaths,
                additionalPaths);
    }

    @Override
    public void startKeywordAutoDiscovering(final int port, final File dataSource,
            final EnvironmentSearchPaths additionalPaths) {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
        executor.startKeywordAutoDiscovering(port, dataSource, additionalPaths);
    }

    @Override
    public void stopAutoDiscovering() {
        final RobotCommandExecutor executor = executors.getRobotCommandExecutor(location);
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
}
