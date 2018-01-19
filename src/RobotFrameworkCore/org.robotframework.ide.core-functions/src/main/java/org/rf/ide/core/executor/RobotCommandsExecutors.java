package org.rf.ide.core.executor;

import org.rf.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;

public interface RobotCommandsExecutors {

    RobotCommandExecutor getRobotCommandExecutor(final PythonInstallationDirectory interpreterPath);

    RobotCommandExecutor getDirectRobotCommandExecutor(PythonInstallationDirectory location);

    void resetExecutorFor(PythonInstallationDirectory location);

}
