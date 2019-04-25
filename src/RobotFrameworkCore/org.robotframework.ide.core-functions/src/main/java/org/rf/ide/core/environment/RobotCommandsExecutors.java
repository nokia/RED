/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.environment;

import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;

public interface RobotCommandsExecutors {

    RobotCommandExecutor getRobotCommandExecutor(PythonInstallationDirectory location);

    void resetExecutorFor(PythonInstallationDirectory location);

}
