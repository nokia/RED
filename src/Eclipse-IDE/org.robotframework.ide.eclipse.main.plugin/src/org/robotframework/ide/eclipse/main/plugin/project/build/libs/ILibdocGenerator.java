/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

interface ILibdocGenerator {

    void generateLibdoc(RobotRuntimeEnvironment runtimeEnvironment, EnvironmentSearchPaths additionalPaths)
            throws RobotEnvironmentException;

    void generateLibdocForcibly(RobotRuntimeEnvironment runtimeEnvironment, EnvironmentSearchPaths additionalPaths)
            throws RobotEnvironmentException;

    String getMessage();

    IFile getTargetFile();

}
