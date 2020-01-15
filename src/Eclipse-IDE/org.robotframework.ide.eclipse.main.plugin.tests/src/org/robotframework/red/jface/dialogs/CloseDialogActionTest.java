/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;

@ExtendWith(FreshShellExtension.class)
public class CloseDialogActionTest {

    @FreshShell
    Shell shell;

    @Test
    public void theShellIsClosedWhenActionRuns() {
        final CloseDialogAction actionToTest = new CloseDialogAction(shell, null);

        assertThat(shell.isDisposed()).isFalse();

        actionToTest.run();

        assertThat(shell.isDisposed()).isTrue();
    }
}
