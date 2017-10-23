/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Michal Anglart
 */
interface RobotCommandExecutor {

    List<File> getModulesSearchPaths();

    Optional<File> getModulePath(String moduleName, EnvironmentSearchPaths additionalPaths);

    List<String> getClassesFromModule(File moduleLocation, String moduleName, EnvironmentSearchPaths additionalPaths);

    Map<String, Object> getVariables(String filePath, List<String> fileArguments);

    Map<String, Object> getGlobalVariables();

    List<String> getStandardLibrariesNames();

    String getStandardLibraryPath(final String libName);

    String getRobotVersion();

    boolean isVirtualenv();

    void createLibdocForStdLibrary(String resultFilePath, String libName, String libPath);

    void createLibdocForThirdPartyLibrary(String resultFilePath, String libName, String libPath,
            EnvironmentSearchPaths additionalPaths);

    void startLibraryAutoDiscovering(int port, List<String> suiteNames, List<String> dataSourcePaths,
            List<String> variableMappings, EnvironmentSearchPaths additionalPaths);

    void stopLibraryAutoDiscovering();

    void runRfLint(final String host, final int port, final File filepath);
}
