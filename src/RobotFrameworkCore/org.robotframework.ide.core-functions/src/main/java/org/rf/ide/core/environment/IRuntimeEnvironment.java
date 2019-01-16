/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.libraries.LibrarySpecification.LibdocFormat;
import org.rf.ide.core.libraries.SitePackagesLibraries;
import org.rf.ide.core.rflint.RfLintRule;

public interface IRuntimeEnvironment {

    boolean isNullEnvironment();

    boolean isValidPythonInstallation();

    boolean hasRobotInstalled();

    boolean isCompatibleRobotInstallation();

    String getVersion();

    RobotVersion getRobotVersion();

    SuiteExecutor getInterpreter();

    File getFile();

    void resetCommandExecutors();

    List<File> getModuleSearchPaths();

    Optional<File> getModulePath(String moduleName, EnvironmentSearchPaths additionalPaths);

    void createLibdoc(String libName, File outputFile, LibdocFormat format, EnvironmentSearchPaths additionalPaths);

    void createLibdocInSeparateProcess(String libName, File outputFile, LibdocFormat format,
            EnvironmentSearchPaths additionalPaths);

    String createHtmlDoc(String doc, DocFormat format);

    List<String> getStandardLibrariesNames();

    Optional<File> getStandardLibraryPath(String libraryName);

    SitePackagesLibraries getSitePackagesLibrariesNames();

    /**
     * Return names of python classes contained in module point by argument and
     * all of its submodules. For packages-module __init__.py file path should
     * be provided.
     *
     * @param moduleLocation
     *            Module location
     * @return list of class names or empty list
     */
    List<String> getClassesFromModule(File moduleLocation, EnvironmentSearchPaths additionalPaths);

    Map<String, Object> getGlobalVariables();

    Map<String, Object> getVariablesFromFile(File source, List<String> arguments);

    void runRfLint(String host, int port, File projectLocation, List<String> excludedPaths, File filepath,
            List<RfLintRule> rules, List<String> rulesFiles, List<String> additionalArguments);

    String convertRobotDataFile(File originalFile);

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
    void startLibraryAutoDiscovering(int port, File dataSource, File projectLocation, boolean recursiveInVirtualenv,
            List<String> excludedPaths, EnvironmentSearchPaths additionalPaths);

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
    void startKeywordAutoDiscovering(int port, File dataSource, EnvironmentSearchPaths additionalPaths);

    void stopAutoDiscovering();

    @SuppressWarnings("serial")
    public static class RuntimeEnvironmentException extends RuntimeException {

        public RuntimeEnvironmentException(final String message) {
            super(message);
        }

        public RuntimeEnvironmentException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
