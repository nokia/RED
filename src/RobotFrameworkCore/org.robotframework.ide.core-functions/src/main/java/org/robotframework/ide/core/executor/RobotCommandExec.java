/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.executor;

import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

/**
 * @author Michal Anglart
 *
 */
interface RobotCommandExec {

    Map<String, Object> getVariables(final String filePath, final String fileArguments);

    Map<String, Object> getGlobalVariables();

    List<String> getStandardLibrariesNames();

    String getStandardLibraryPath(final String libName);

    String getRobotVersion();

    String getRunModulePath();

    void createLibdocForStdLibrary(final String resultFilePath, final String libName, final String libPath)
            throws RobotEnvironmentException;

    void createLibdocForPythonLibrary(final String resultFilePath, final String libName, final String libPath)
            throws RobotEnvironmentException;

    void createLibdocForJavaLibrary(final String resultFilePath, final String libName, final String libPath)
            throws RobotEnvironmentException;
}
