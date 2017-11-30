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
import org.eclipse.ui.console.IPatternMatchListener;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.red.swt.SwtThread;

/**
 * @author Michal Anglart
 *
 */
public class RobotConsoleFacade {

    private final IOConsole console;

    private final IOConsoleOutputStream stream;

    public RobotConsoleFacade(final IOConsole console, final IOConsoleOutputStream stream) {
        this.console = console;
        this.stream = stream;
    }

    public static RobotConsoleFacade provide(final ILaunchConfiguration launchConfiguration,
            final String consoleDescription) {
        final IOConsole console = getConsole(LaunchConfigurationsWrappers.robotLaunchConfiguration(launchConfiguration),
                consoleDescription);
        if (console == null) {
            throw new IllegalStateException("Unable to find output console");
        }
        final IOConsoleOutputStream stream = console.newOutputStream();
        SwtThread.syncExec(() -> stream.setColor(RedTheme.Colors.getRobotConsoleRedStreamColor()));
        return new RobotConsoleFacade(console, stream);
    }

    private static IOConsole getConsole(final IRobotLaunchConfiguration robotConfig, final String description) {
        final String consoleName = robotConfig.getName() + " [" + robotConfig.getTypeName() + "] " + description;
        final IConsole[] existingConsoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
        for (final IConsole console : existingConsoles) {
            if (console instanceof IOConsole && console.getName().contains(consoleName)) {
                return (IOConsole) console;
            }
        }
        return null;
    }

    public void writeLine(final String line) throws IOException {
        stream.write(line + "\n");
    }

    public void addHyperlinksSupport(final IPatternMatchListener consolePatternsMatchListener) {
        console.addPatternMatchListener(consolePatternsMatchListener);
    }
}
