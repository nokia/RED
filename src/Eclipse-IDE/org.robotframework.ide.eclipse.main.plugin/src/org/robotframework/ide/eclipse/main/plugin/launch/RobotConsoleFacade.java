/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.io.IOException;
import java.util.function.Supplier;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.IPatternMatchListener;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.red.swt.SwtThread;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 */
public class RobotConsoleFacade {

    private final IOConsole console;

    private final IOConsoleOutputStream stream;

    private RobotConsoleFacade(final IOConsole console, final IOConsoleOutputStream stream) {
        this.console = console;
        this.stream = stream;
    }

    public static RobotConsoleFacade provide(final ILaunchConfiguration launchConfiguration, final String description) {
        final IOConsole console = findConsole(launchConfiguration, description,
                () -> ConsolePlugin.getDefault().getConsoleManager().getConsoles(), 5_000);
        final IOConsoleOutputStream stream = console.newOutputStream();
        SwtThread.syncExec(() -> stream.setColor(RedTheme.Colors.getRobotConsoleRedStreamColor()));
        return new RobotConsoleFacade(console, stream);
    }

    @VisibleForTesting
    static IOConsole findConsole(final ILaunchConfiguration launchConfiguration, final String description,
            final Supplier<IConsole[]> consolesSupplier, final int timeout) {
        final IRobotLaunchConfiguration robotConfig = LaunchConfigurationsWrappers
                .robotLaunchConfiguration(launchConfiguration);
        final String consoleName = robotConfig.getName() + " [" + robotConfig.getTypeName() + "] " + description;

        // since 4.10 console creation mechanism was changed from a synchronous to an asynchronous
        final long start = System.currentTimeMillis();
        while (true) {
            final IConsole[] existingConsoles = consolesSupplier.get();
            for (final IConsole console : existingConsoles) {
                if (console instanceof IOConsole && console.getName().contains(consoleName)) {
                    return (IOConsole) console;
                }
            }

            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                // we'll try once again
            }

            // to avoid infinite loops
            if (System.currentTimeMillis() - start > timeout) {
                throw new IllegalStateException("Unable to find output console");
            }
        }
    }

    public void writeLine(final String line) throws IOException {
        stream.write(line + "\n");
    }

    public void addHyperlinksSupport(final IPatternMatchListener consolePatternsMatchListener) {
        console.addPatternMatchListener(consolePatternsMatchListener);
    }
}
