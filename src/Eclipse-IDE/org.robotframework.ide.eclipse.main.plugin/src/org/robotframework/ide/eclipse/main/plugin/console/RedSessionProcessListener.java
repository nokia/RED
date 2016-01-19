/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.console;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.rf.ide.core.executor.PythonProcessListener;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

/**
 * @author Michal Anglart
 *
 */
public final class RedSessionProcessListener implements PythonProcessListener {

    private final Map<Process, RedSessionConsole> streams = new ConcurrentHashMap<>();

    @Override
    public void processStarted(final String name, final Process process) {
        final RedSessionConsole console = SwtThread.syncEval(new Evaluation<RedSessionConsole>() {
            @Override
            public RedSessionConsole runCalculation() {
                final RedSessionConsole console = openRobotServerConsole(name, process);
                console.initializeStreams();
                return console;
            }
        });
        streams.put(process, console);
    }

    @Override
    public void processEnded(final Process process) {
        final RedSessionConsole console = streams.remove(process);
        if (console != null) {
            SwtThread.asyncExec(new Runnable() {
                @Override
                public void run() {
                    console.processTerminated();
                }
            });
        }
    }

    @Override
    public void lineRead(final Process serverProcess, final String line) {
        final RedSessionConsole console = streams.get(serverProcess);
        if (console != null) {
            console.getStdOutStream().println(line);
        }
    }

    @Override
    public void errorLineRead(final Process serverProcess, final String line) {
        final RedSessionConsole console = streams.get(serverProcess);
        if (console != null) {
            console.getStdErrStream().println(line);
        }
    }

    private static RedSessionConsole openRobotServerConsole(final String interpreterName, final Process process) {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            final IConsoleView consoleView = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
            final String name = createName(interpreterName);
            RedSessionConsole console = findConsole(name);
            if (console == null) {
                console = createConsole(name, process);
            }
            consoleView.display(console);

            return console;
        } catch (final PartInitException e) {
            return null;
        }
    }

    private static String createName(final String interpreterName) {
        return "RED session [" + interpreterName + "]";
    }

    private static RedSessionConsole findConsole(final String name) {
        final IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        final IConsole[] existing = consoleManager.getConsoles();
        for (final IConsole console : existing) {
            if (name.equals(console.getName())) {
                return (RedSessionConsole) console;
            }
        }
        return null;
    }

    private static RedSessionConsole createConsole(final String name, final Process process) {
        final IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        final RedSessionConsole console = new RedSessionConsole(name, process);
        consoleManager.addConsoles(new IConsole[] { console });
        return console;
    }
}