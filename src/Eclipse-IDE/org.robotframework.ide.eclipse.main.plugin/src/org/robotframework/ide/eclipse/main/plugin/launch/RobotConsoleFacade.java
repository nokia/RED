/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.io.IOException;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RunCommandLine;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public class RobotConsoleFacade {

    private Optional<IOConsoleOutputStream> redMessagesStream;

    void connect(final ILaunchConfiguration configuration, final RobotRuntimeEnvironment runtimeEnvironment,
            final SuiteExecutor executor, final RunCommandLine cmdLine) throws IOException {
        final Optional<IOConsole> cons = getConsole(configuration, runtimeEnvironment.getFile().getAbsolutePath());
        if (cons.isPresent()) {
            cons.get().addPatternMatchListener(new RobotConsolePatternsListener());
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
        printCommandOnConsole(redMessagesStream, cmdLine.getCommandLine(), runtimeEnvironment.getVersion(executor));
    }

    private Optional<IOConsole> getConsole(final ILaunchConfiguration configuration, final String description)
            throws IOException {
        final String consoleName = configuration.getName() + " [Robot] " + description;
        final IConsole[] existingConsoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
        for (final IConsole console : existingConsoles) {
            if (console instanceof IOConsole && console.getName().contains(consoleName)) {
                return Optional.of((IOConsole) console);
            }
        }
        return Optional.absent();
    }

    private static void printCommandOnConsole(final Optional<IOConsoleOutputStream> redMessagesStream,
            final String[] commandLine, final String version) throws IOException {
        if (redMessagesStream.isPresent()) {
            final String command = "Command: " + Joiner.on(' ').join(commandLine) + "\n";
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
