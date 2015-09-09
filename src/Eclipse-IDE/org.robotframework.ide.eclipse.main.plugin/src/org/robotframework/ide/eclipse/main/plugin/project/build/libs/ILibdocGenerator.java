/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

interface ILibdocGenerator {

    void generateLibdoc(RobotRuntimeEnvironment runtimeEnvironment) throws RobotEnvironmentException;

    String getMessage();

}
