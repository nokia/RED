/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.swt.SwtThread;

public class PathEntryDialogTest {

    @Rule
    public ShellProvider shell = new ShellProvider();

    @Test
    public void entryDialogProperlyGeneratesSearchPaths_whenPathsAreProvidedInTextBox() throws Exception {
        final AtomicBoolean finished = new AtomicBoolean(false);
        final AtomicReference<PathEntryDialog> dialog = new AtomicReference<>(null);
        
        final Thread guiChangesRequestingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SwtThread.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        dialog.set(new PathEntryDialog(shell.getShell()));
                        dialog.get().open();
                        finished.set(true);
                    }
                });
                SwtThread.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        dialog.get().getSearchPathsText().setText("path1\n\n  \t  \npath2\n");
                        dialog.get().getOkButton().notifyListeners(SWT.Selection, new Event());
                    }
                });
            }
        });
        guiChangesRequestingThread.start();
        guiChangesRequestingThread.join();

        while (!finished.get()) {
            Thread.sleep(100);
            while (shell.getShell().getDisplay().readAndDispatch()) {
            }
        }

        assertThat(dialog.get().getSearchPath()).containsExactly(SearchPath.create("path1"),
                SearchPath.create("path2"));
    }
}
