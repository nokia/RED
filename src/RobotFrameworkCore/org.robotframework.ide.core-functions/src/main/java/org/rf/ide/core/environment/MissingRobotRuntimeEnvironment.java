/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.libraries.LibrarySpecification.LibdocFormat;
import org.rf.ide.core.libraries.SitePackagesLibraries;
import org.rf.ide.core.rflint.RfLintRule;

public class MissingRobotRuntimeEnvironment extends RobotRuntimeEnvironment {

    public MissingRobotRuntimeEnvironment(final PythonInstallationDirectory location) {
        super(location, null);
    }

    @Override
    public boolean hasRobotInstalled() {
        return false;
    }

    @Override
    public boolean isCompatibleRobotInstallation() {
        return false;
    }

    @Override
    public String getVersion() {
        return "<unknown>";
    }

    @Override
    public RobotVersion getRobotVersion() {
        return RobotVersion.UNKNOWN;
    }

    @Override
    public void resetCommandExecutors() {
        // nothing to do
    }

    @Override
    public List<File> getModuleSearchPaths() {
        return new ArrayList<>();
    }

    @Override
    public Optional<File> getModulePath(final String moduleName, final EnvironmentSearchPaths additionalPaths) {
        return Optional.empty();
    }

    @Override
    public void createLibdoc(final String libName, final File outputFile, final LibdocFormat format,
            final EnvironmentSearchPaths additionalPaths) {
        // nothing to do
    }

    @Override
    public void createLibdocInSeparateProcess(final String libName, final File outputFile, final LibdocFormat format,
            final EnvironmentSearchPaths additionalPaths) {
        // nothing to do
    }

    @Override
    public String createHtmlDoc(final String doc, final DocFormat format) {
        return "";
    }

    @Override
    public List<String> getStandardLibrariesNames() {
        return new ArrayList<>();
    }

    @Override
    public Optional<File> getStandardLibraryPath(final String libraryName) {
        return Optional.empty();
    }

    @Override
    public SitePackagesLibraries getSitePackagesLibrariesNames() {
        return new SitePackagesLibraries();
    }

    @Override
    public List<String> getClassesFromModule(final File moduleLocation, final EnvironmentSearchPaths additionalPaths) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getGlobalVariables() {
        return new LinkedHashMap<>();
    }

    @Override
    public Map<String, Object> getVariablesFromFile(final File source, final List<String> arguments) {
        return new LinkedHashMap<>();
    }

    @Override
    public void runRfLint(final String host, final int port, final File projectLocation,
            final List<String> excludedPaths, final File filepath, final List<RfLintRule> rules,
            final List<String> rulesFiles, final List<String> additionalArguments) {
        // nothing to do
    }

    @Override
    public String convertRobotDataFile(final File originalFile) {
        return "";
    }

    @Override
    public void startLibraryAutoDiscovering(final int port, final File dataSource, final File projectLocation,
            final boolean recursiveInVirtualenv, final List<String> excludedPaths,
            final EnvironmentSearchPaths additionalPaths) {
        // nothing to do
    }

    @Override
    public void startKeywordAutoDiscovering(final int port, final File dataSource,
            final EnvironmentSearchPaths additionalPaths) {
        // nothing to do
    }

    @Override
    public void stopAutoDiscovering() {
        // nothing to do
    }

}
