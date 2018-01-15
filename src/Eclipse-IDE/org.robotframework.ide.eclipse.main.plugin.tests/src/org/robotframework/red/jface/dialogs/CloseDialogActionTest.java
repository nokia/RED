/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.widgets.Shell;
import org.junit.Rule;
import org.junit.Test;
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
}
