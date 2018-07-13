/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.executor;

import org.rf.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;

public interface RobotCommandsExecutors {

    RobotCommandExecutor getRobotCommandExecutor(PythonInstallationDirectory interpreterPath);

    void resetExecutorFor(PythonInstallationDirectory location);

}
