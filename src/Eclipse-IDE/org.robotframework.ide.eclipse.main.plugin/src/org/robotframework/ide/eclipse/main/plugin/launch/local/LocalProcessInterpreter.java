/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.environment.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.annotations.VisibleForTesting;

class LocalProcessInterpreter {

    private final SuiteExecutor executor;

    private final String path;

    private final String version;

    @VisibleForTesting
    LocalProcessInterpreter(final SuiteExecutor executor, final String path, final String version) {
        this.executor = executor;
        this.path = path;
        this.version = version;
    }

    SuiteExecutor getExecutor() {
        return executor;
    }

    String getPath() {
        return path;
    }

    String getVersion() {
        return version;
    }

    static LocalProcessInterpreter create(final RobotLaunchConfiguration robotConfig, final RobotProject robotProject)
            throws CoreException {
        if (robotConfig.isUsingInterpreterFromProject()) {
            return LocalProcessInterpreter.create(robotProject.getRuntimeEnvironment(), robotProject.getName());
        }
        return LocalProcessInterpreter.create(robotConfig.getInterpreter());
    }

    private static LocalProcessInterpreter create(final IRuntimeEnvironment env, final String projectName)
            throws CoreException {
        if (env.isNullEnvironment()) {
            throw newCoreException("There is no active runtime environment for project '" + projectName + "'");
        } else if (!env.isValidPythonInstallation()) {
            throw newCoreException(
                    "The runtime environment " + env.getFile().getAbsolutePath() + " is invalid Python installation");
        } else if (!env.hasRobotInstalled()) {
            throw newCoreException(
                    "The runtime environment " + env.getFile().getAbsolutePath() + " has no Robot Framework installed");
        }
        return new LocalProcessInterpreter(env.getInterpreter(), findPythonExecutablePath(env), env.getVersion());
    }

    private static String findPythonExecutablePath(final IRuntimeEnvironment env) {
        final SuiteExecutor interpreter = env.getInterpreter();
        final String pythonExec = interpreter.executableName();
        return Stream.of(env.getFile().listFiles())
                .filter(file -> pythonExec.equals(file.getName()))
                .findFirst()
                .map(File::getAbsolutePath)
                .orElse(pythonExec);
    }

    private static LocalProcessInterpreter create(final SuiteExecutor interpreter) throws CoreException {
        final Optional<PythonInstallationDirectory> installation = PythonInstallationDirectoryFinder
                .whereIsPythonInterpreter(interpreter);
        if (installation.isPresent()) {
            final Optional<String> robotVersion = installation.flatMap(PythonInstallationDirectory::getRobotVersion);
            if (robotVersion.isPresent()) {
                return new LocalProcessInterpreter(interpreter, interpreter.executableName(), robotVersion.get());
            } else {
                throw newCoreException("The " + interpreter.name() + " interpreter has no Robot Framework installed");
            }
        } else {
            throw newCoreException(
                    "There is no " + interpreter.name() + " interpreter in system PATH environment variable");
        }
    }

}
