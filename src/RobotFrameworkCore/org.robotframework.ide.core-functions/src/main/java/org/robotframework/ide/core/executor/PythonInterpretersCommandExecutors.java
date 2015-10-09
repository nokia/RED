/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.executor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.robotframework.ide.core.executor.RobotCommandRcpExecutor.RobotCommandExecutorException;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;

/**
 * @author Michal Anglart
 *
 */
class PythonInterpretersCommandExecutors {

    private static class InstanceHolder {
        private static final PythonInterpretersCommandExecutors INSTANCE = new PythonInterpretersCommandExecutors();
    }

    static PythonInterpretersCommandExecutors getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final Map<String, RobotCommandRcpExecutor> executors = new HashMap<>();

    private File xmlRpcServerScriptFile;

    private PythonInterpretersCommandExecutors() {
        try {
            xmlRpcServerScriptFile = RobotRuntimeEnvironment.copyResourceFile("RobotCommandExecutionServer.py");
        } catch (final IOException e) {
            xmlRpcServerScriptFile = null;
        }
    }

    synchronized RobotCommandExecutor getRobotCommandExecutor(final PythonInstallationDirectory interpreterPath) {
        final String pathAsName = interpreterPath.toPath()
                .resolve(interpreterPath.getInterpreter().executableName())
                .toAbsolutePath()
                .toString();
        
        if (xmlRpcServerScriptFile == null) {
            return new RobotCommandDirectExecutor(pathAsName);
        }

        RobotCommandRcpExecutor executor = executors.get(pathAsName);
        if (executor != null && executor.isAlive()) {
            return executor;
        } else if (executor != null) {
            executor.kill();
            executors.remove(pathAsName);
        }
        try {
            executor = new RobotCommandRcpExecutor(pathAsName, xmlRpcServerScriptFile);
            executor.waitForEstablishedConnection();
            if (executor.isAlive()) {
                executors.put(pathAsName, executor);
                return executor;
            } else {
                return new RobotCommandDirectExecutor(pathAsName);
            }
        } catch (final RobotCommandExecutorException e) {
            return new RobotCommandDirectExecutor(pathAsName);
        }
    }

}
