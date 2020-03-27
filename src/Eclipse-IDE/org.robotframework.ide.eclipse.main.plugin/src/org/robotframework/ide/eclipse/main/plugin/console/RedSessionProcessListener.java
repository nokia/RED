/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.console;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.rf.ide.core.environment.PythonProcessListener;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Futures;

/**
 * @author Michal Anglart
 *
 */
public final class RedSessionProcessListener implements PythonProcessListener {

    private final Map<Process, Future<RedSessionConsole>> streams = new ConcurrentHashMap<>();

    private final IConsoleManager consoleManager;

    public RedSessionProcessListener() {
        this(ConsolePlugin.getDefault().getConsoleManager());
    }

    @VisibleForTesting
    RedSessionProcessListener(final IConsoleManager consoleManager) {
        this.consoleManager = consoleManager;
    }

    @VisibleForTesting
    Map<Process, Future<RedSessionConsole>> getCurrentProcesses() {
        return streams;
    }

    @Override
    public void processStarted(final String name, final Process process) {
        if (SwtThread.isSwtThread()) {
            streams.put(process, Futures.immediateFuture(openAndInitializeConsole(name, process)));
        } else {
            streams.put(process, SwtThread.asyncEval(Evaluation.of(() -> openAndInitializeConsole(name, process))));
        }
    }

    private RedSessionConsole openAndInitializeConsole(final String name, final Process process) {
        final RedSessionConsole console = openRobotServerConsole(name, process);
        console.initializeStreams();
        return console;
    }

    @Override
    public void processEnded(final Process process) {
        final Future<RedSessionConsole> console = streams.remove(process);
        if (console != null) {
            SwtThread.asyncExec(() -> {
                try {
                    console.get().processTerminated();
                } catch (InterruptedException | ExecutionException e) {
                    throw new IllegalStateException(e);
                }
            });
        }
    }

    @Override
    public void lineRead(final Process serverProcess, final String line) {
        final Future<RedSessionConsole> console = streams.get(serverProcess);
        if (console != null) {
            try {
                console.get().getStdOutStream().println(line);
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void errorLineRead(final Process serverProcess, final String line) {
        final Future<RedSessionConsole> console = streams.get(serverProcess);
        if (console != null) {
            try {
                console.get().getStdErrStream().println(line);
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private RedSessionConsole openRobotServerConsole(final String interpreterName, final Process process) {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
        final IWorkbenchPage page = activeWindow.getActivePage();
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

    private RedSessionConsole findConsole(final String name) {
        return Stream.of(consoleManager.getConsoles())
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .map(RedSessionConsole.class::cast)
                .orElse(null);
    }

    private RedSessionConsole createConsole(final String name, final Process process) {
        final RedSessionConsole console = new RedSessionConsole(name, process);
        consoleManager.addConsoles(new IConsole[] { console });
        return console;
    }
}