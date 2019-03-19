/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

public class ProgressMonitorDialogWithConsoleTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void dialogHasStyledTextControl() {
        final ProgressMonitorDialogWithConsole dialog = new ProgressMonitorDialogWithConsole(shellProvider.getShell());
        dialog.open();

        final Shell shell = dialog.getShell();

        assertThat(shell.getChildren()).hasAtLeastOneElementOfType(StyledText.class);
        assertThat(getStyledText(shell).getText()).isEmpty();
    }

    @Test
    public void dialogIsClosedWhenOperationFinishes() throws Exception {
        final ProgressMonitorDialogWithConsole dialog = new ProgressMonitorDialogWithConsole(shellProvider.getShell());
        dialog.open();

        final Shell shell = dialog.getShell();
        
        dialog.run((monitor, output) -> { });

        assertThat(shell.isDisposed()).isTrue();
    }
    
    @Test
    public void dialogIsNotClosedWhenOperationMarksItCannotBeClosed_butIsClosedWhenDoneAgain() throws Exception {
        final ProgressMonitorDialogWithConsole dialog = new ProgressMonitorDialogWithConsole(shellProvider.getShell());
        dialog.open();

        final Shell shell = dialog.getShell();
        
        dialog.run((monitor, output) -> dialog.markDoNotClose());

        assertThat(shell.isDisposed()).isFalse();
        dialog.close();
        assertThat(shell.isDisposed()).isTrue();
    }

    @Test
    public void dialogHasTheTextGeneratedByScheduledOperation() throws Exception {
        final ProgressMonitorDialogWithConsole dialog = new ProgressMonitorDialogWithConsole(shellProvider.getShell());
        dialog.open();

        final Shell shell = dialog.getShell();
        final StyledText styledText = getStyledText(shell);
        
        dialog.run((monitor, output) -> {
            output.accept("foo");
            output.accept("bar");
            dialog.markDoNotClose();
        });
        
        assertThat(styledText.getText()).isEqualTo("foo" + System.lineSeparator() + "bar");

        dialog.close();
    }

    private static StyledText getStyledText(final Composite parent) {
        return Stream.of(parent.getChildren())
                .filter(StyledText.class::isInstance)
                .map(StyledText.class::cast)
                .findFirst()
                .get();
    }
}
