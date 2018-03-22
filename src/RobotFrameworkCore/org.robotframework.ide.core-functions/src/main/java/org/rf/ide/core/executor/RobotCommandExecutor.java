/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.executor.RobotRuntimeEnvironment.LibdocFormat;
import org.rf.ide.core.rflint.RfLintRule;

/**
 * @author Michal Anglart
 */
interface RobotCommandExecutor {

    List<File> getModulesSearchPaths();

    File getModulePath(String moduleName, EnvironmentSearchPaths additionalPaths);

    List<String> getClassesFromModule(File moduleLocation, EnvironmentSearchPaths additionalPaths);

    Map<String, Object> getVariables(String filePath, List<String> fileArguments);

    Map<String, Object> getGlobalVariables();

    List<String> getStandardLibrariesNames();

    String getStandardLibraryPath(final String libName);

    String getRobotVersion();

    void createLibdoc(String resultFilePath, LibdocFormat format, String libName, String libPath,
            EnvironmentSearchPaths additionalPaths);

    void startLibraryAutoDiscovering(int port, File dataSource, File projectLocation, boolean recursiveInVirtualenv,
            List<String> excludedPaths, EnvironmentSearchPaths additionalPaths);

    void startKeywordAutoDiscovering(int port, File dataSource, EnvironmentSearchPaths additionalPaths);

    void stopAutoDiscovering();

    void runRfLint(String host, int port, File projectLocation, List<String> excludedPaths, File filepath,
            List<RfLintRule> rules, List<String> rulesFiles, List<String> additionalArguments);
}
