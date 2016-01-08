/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.widgets.Shell;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.jface.dialogs.CloseDialogAction.DialogCloseListener;
import org.robotframework.red.junit.ShellProvider;

public class CloseDialogActionTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void theShellIsClosedWhenActionRuns() {
        final Shell shell = shellProvider.getShell();
        final CloseDialogAction actionToTest = new CloseDialogAction(shell, null);

        assertThat(shell.isDisposed()).isFalse();

        actionToTest.run();

        assertThat(shell.isDisposed()).isTrue();
    }

    @Test
    public void theListenerIsNotifiedPriorToClosing() {
        final AtomicBoolean beforeCalled = new AtomicBoolean(false);

        final Shell shell = shellProvider.getShell();
        final DialogCloseListener listener = new DialogCloseListener() {
            @Override
            public void beforeClose() {
                assertThat(shell.isDisposed()).isFalse();
                beforeCalled.set(true);
            }
        };

        new CloseDialogAction(shell, null, listener).run();

        assertThat(beforeCalled.get()).isTrue();
    }

    @Test
    public void theListenerIsNotifiedJustAfterClosing() {
        final AtomicBoolean afterCalled = new AtomicBoolean(false);

        final Shell shell = shellProvider.getShell();
        final DialogCloseListener listener = new DialogCloseListener() {
            @Override
            public void afterClose() {
                assertThat(shell.isDisposed()).isTrue();
                afterCalled.set(true);
            }
        };

        new CloseDialogAction(shell, null, listener).run();

        assertThat(afterCalled.get()).isTrue();
    }

}
