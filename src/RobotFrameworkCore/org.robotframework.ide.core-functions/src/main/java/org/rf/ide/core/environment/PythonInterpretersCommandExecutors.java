/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.RedSystemProperties;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.environment.RobotCommandRpcExecutor.ExternalRobotCommandRpcExecutor;
import org.rf.ide.core.environment.RobotCommandRpcExecutor.InternalRobotCommandRpcExecutor;

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

    private final List<PythonProcessListener> processListeners = new ArrayList<>();

    private PythonInterpretersCommandExecutors() {
        // instance of this class should not be created outside
    }

    synchronized List<PythonProcessListener> getListeners() {
        // copy for avoiding concurrent modification
        return Collections.unmodifiableList(new ArrayList<>(processListeners));
    }

    synchronized void addProcessListener(final PythonProcessListener listener) {
        processListeners.add(listener);
    }

    synchronized void removeProcessListener(final PythonProcessListener listener) {
        processListeners.remove(listener);
    }

    @Override
    public synchronized void resetExecutorFor(final PythonInstallationDirectory location) {
        final String interpreterPath = location.getInterpreterPath();
        final RobotCommandRpcExecutor executor = executors.remove(interpreterPath);
        if (executor != null) {
            executor.kill();
        }
    }

    @Override
    public synchronized RobotCommandExecutor getRobotCommandExecutor(final PythonInstallationDirectory location) {
        RobotCommandRpcExecutor executor = executors.get(location.getInterpreterPath());
        if (executor == null || !executor.isAlive()) {
            executor = RedSystemProperties.shouldConnectToRunningServer()
                    ? new ExternalRobotCommandRpcExecutor(location.getInterpreter(),
                            RedSystemProperties.getSessionServerAddress())
                    : new InternalRobotCommandRpcExecutor(location.getInterpreter(), location.getInterpreterPath(),
                            this::getListeners);
            executor.initialize();
            executor.establishConnection();
            executors.put(location.getInterpreterPath(), executor);
        } else {
            executor.initialize();
        }
        return executor;
    }
}
