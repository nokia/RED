/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
interface RobotCommandExecutor {

    List<File> getModulesSearchPaths();

    Optional<File> getModulePath(String moduleName);

    Map<String, Object> getVariables(final String filePath, final List<String> fileArguments);

    Map<String, Object> getGlobalVariables();

    List<String> getStandardLibrariesNames();

    String getStandardLibraryPath(final String libName);

    String getRobotVersion();

    String getRunModulePath();

    void createLibdocForStdLibrary(final String resultFilePath, final String libName, final String libPath);

    void createLibdocForPythonLibrary(final String resultFilePath, final String libName, final String libPath);

    void createLibdocForJavaLibrary(final String resultFilePath, final String libName, final String libPath);
}
