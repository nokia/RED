/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.RedTemporaryDirectory;
import org.rf.ide.core.executor.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.executor.RobotCommandRpcExecutor.RobotCommandExecutorException;

/**
 * @author Michal Anglart
 */
class PythonInterpretersCommandExecutors implements RobotCommandsExecutors {

    private static final List<String> SCRIPT_FILES = Arrays.asList("robot_session_server.py", "classpath_updater.py",
            "red_keyword_autodiscover.py", "red_libraries.py", "red_library_autodiscover.py", "red_module_classes.py",
            "red_modules.py", "red_variables.py", "rflint_integration.py", "SuiteVisitorImportProxy.py",
            "TestRunnerAgent.py");

    private static class InstanceHolder {

        private static final PythonInterpretersCommandExecutors INSTANCE = new PythonInterpretersCommandExecutors();
    }

    static PythonInterpretersCommandExecutors getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final Map<String, RobotCommandRpcExecutor> executors = new HashMap<>();

    private final List<PythonProcessListener> processListeners = new ArrayList<>();

    private List<File> xmlRpcServerScriptFiles;

    private PythonInterpretersCommandExecutors() {
        initializeScripts();
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
        final String pathAsName = interpreterPath.getInterpreterPath();
        final RobotCommandRpcExecutor executor = executors.remove(pathAsName);
        if (executor != null) {
            executor.kill();
        }
    }

    @Override
    public synchronized RobotCommandExecutor getRobotCommandExecutor(
            final PythonInstallationDirectory interpreterPath) {
        if (!xmlRpcServerScriptFiles.stream().allMatch(File::exists)) {
            initializeScripts();
        }

        final String pathAsName = interpreterPath.getInterpreterPath();
        if (new File(pathAsName).exists()) {
            RobotCommandRpcExecutor executor = executors.get(pathAsName);
            if (executor != null && (executor.isAlive() || executor.isExternal())) {
                return executor;
            } else if (executor != null) {
                executors.remove(pathAsName);
            }
            executor = new RobotCommandRpcExecutor(pathAsName, interpreterPath.getInterpreter(),
                    xmlRpcServerScriptFiles.get(0));
            executor.waitForEstablishedConnection();
            if (executor.isAlive() || executor.isExternal()) {
                executors.put(pathAsName, executor);
                return executor;
            }
        }

        throw new RobotCommandExecutorException("Unable to start XML-RPC server on file: " + interpreterPath);
    }

    private void initializeScripts() {
        try {
            xmlRpcServerScriptFiles = new ArrayList<>();
            for (final String scriptFile : SCRIPT_FILES) {
                xmlRpcServerScriptFiles.add(RedTemporaryDirectory.copyScriptFile(scriptFile));
            }
        } catch (final IOException e) {
            throw new RobotCommandExecutorException("Unable to create temporary directory for XML-RPC server", e);
        }
    }
}
