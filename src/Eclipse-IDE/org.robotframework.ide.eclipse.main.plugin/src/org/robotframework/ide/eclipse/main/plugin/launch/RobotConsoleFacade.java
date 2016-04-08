/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public class RobotConsoleFacade {

    private Optional<IOConsoleOutputStream> redMessagesStream;

    void connect(final RobotLaunchConfiguration robotConfig, final RobotRuntimeEnvironment runtimeEnvironment,
            final RunCommandLine cmdLine, final String version) throws IOException, CoreException {
        final Optional<IOConsole> cons = getConsole(robotConfig,
                robotConfig.createConsoleDescription(runtimeEnvironment));
        if (cons.isPresent()) {
            cons.get().addPatternMatchListener(new RobotConsolePatternsListener(robotConfig.getRobotProject()));
            redMessagesStream = Optional.of(cons.get().newOutputStream());
        } else {
            redMessagesStream = Optional.absent();
        }
        if (redMessagesStream.isPresent()) {
            SwtThread.syncExec(new Runnable() {
                @Override
                public void run() {
                    redMessagesStream.get().setColor(RedTheme.getRobotConsoleRedStreamColor());
                }
            });
        }
        printCommandOnConsole(redMessagesStream, cmdLine, version);
    }

    private Optional<IOConsole> getConsole(final RobotLaunchConfiguration robotConfig, final String description)
            throws IOException {
        final String consoleName = robotConfig.getName() + " [Robot] " + description;
        final IConsole[] existingConsoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
        for (final IConsole console : existingConsoles) {
            if (console instanceof IOConsole && console.getName().contains(consoleName)) {
                return Optional.of((IOConsole) console);
            }
        }
        return Optional.absent();
    }

    private static void printCommandOnConsole(final Optional<IOConsoleOutputStream> redMessagesStream,
            final RunCommandLine cmdLine, final String version) throws IOException {
        if (redMessagesStream.isPresent()) {
            final String command = "Command: " + cmdLine.show() + "\n";
            final String env = "Suite Executor: " + version + "\n";
            redMessagesStream.get().write(command + env);
        }
    }

    public void writeLine(final String line) throws IOException {
        if (redMessagesStream.isPresent()) {
            redMessagesStream.get().write(line + "\n");
        }
    }
}
