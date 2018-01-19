/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.executor.RobotCommandRpcExecutor.RobotCommandExecutorException;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;

/**
 * @author Michal Anglart
 */
class PythonInterpretersCommandExecutors implements RobotCommandsExecutors {

    private static class InstanceHolder {

        private static final PythonInterpretersCommandExecutors INSTANCE = new PythonInterpretersCommandExecutors();
    }

    static PythonInterpretersCommandExecutors getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final Map<String, RobotCommandRpcExecutor> executors = new HashMap<>();

    private final List<PythonProcessListener> processListeners = new ArrayList<>(0);

    private File xmlRpcServerScriptFile;

    private PythonInterpretersCommandExecutors() {
        try {
            xmlRpcServerScriptFile = RobotRuntimeEnvironment.copyScriptFile("robot_session_server.py");
            RobotRuntimeEnvironment.copyScriptFile("classpath_updater.py");
            RobotRuntimeEnvironment.copyScriptFile("red_keyword_autodiscover.py");
            RobotRuntimeEnvironment.copyScriptFile("red_libraries.py");
            RobotRuntimeEnvironment.copyScriptFile("red_library_autodiscover.py");
            RobotRuntimeEnvironment.copyScriptFile("red_module_classes.py");
            RobotRuntimeEnvironment.copyScriptFile("red_modules.py");
            RobotRuntimeEnvironment.copyScriptFile("red_variables.py");
            RobotRuntimeEnvironment.copyScriptFile("rflint_integration.py");
            RobotRuntimeEnvironment.copyScriptFile("SuiteVisitorImportProxy.py");
            RobotRuntimeEnvironment.copyScriptFile("TestRunnerAgent.py");
        } catch (final IOException e) {
            xmlRpcServerScriptFile = null;
        }
    }

    List<PythonProcessListener> getListeners() {
        return processListeners;
    }

    void addProcessListener(final PythonProcessListener listener) {
        processListeners.add(listener);
    }

    void removeProcessListener(final PythonProcessListener listener) {
        processListeners.remove(listener);
    }

    @Override
    public synchronized void resetExecutorFor(final PythonInstallationDirectory interpreterPath) {
        final String pathAsName = interpreterPath.toPath()
                .resolve(interpreterPath.getInterpreter().executableName())
                .toAbsolutePath()
                .toString();
        final RobotCommandRpcExecutor executor = executors.remove(pathAsName);
        if (executor != null) {
            executor.kill();
        }
    }

    @Override
    public synchronized RobotCommandExecutor getRobotCommandExecutor(
            final PythonInstallationDirectory interpreterPath) {
        final SuiteExecutor interpreter = interpreterPath.getInterpreter();
        final String pathAsName = interpreterPath.toPath()
                .resolve(interpreter.executableName())
                .toAbsolutePath()
                .toString();

        if (!RedSystemProperties.shouldUseDirectExecutor() && xmlRpcServerScriptFile != null) {
            RobotCommandRpcExecutor executor = executors.get(pathAsName);
            if (executor != null && (executor.isAlive() || executor.isExternal())) {
                return executor;
            } else if (executor != null) {
                executors.remove(pathAsName);
            }
            try {
                executor = new RobotCommandRpcExecutor(pathAsName, interpreter, xmlRpcServerScriptFile);
                executor.waitForEstablishedConnection();
                if (executor.isAlive() || executor.isExternal()) {
                    executors.put(pathAsName, executor);
                    return executor;
                }
            } catch (final RobotCommandExecutorException e) {
                // direct executor will be returned
            }
        }

        return new RobotCommandDirectExecutor(pathAsName, interpreter);
    }

    @Override
    public RobotCommandExecutor getDirectRobotCommandExecutor(final PythonInstallationDirectory interpreterPath) {
        final String pathAsName = interpreterPath.toPath()
                .resolve(interpreterPath.getInterpreter().executableName())
                .toAbsolutePath()
                .toString();
        return new RobotCommandDirectExecutor(pathAsName, interpreterPath.getInterpreter());
    }
}
