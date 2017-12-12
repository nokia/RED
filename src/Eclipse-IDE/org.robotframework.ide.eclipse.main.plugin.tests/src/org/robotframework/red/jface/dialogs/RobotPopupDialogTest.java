/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.dialogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

public class RobotPopupDialogTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void whenDialogIsCreated_theParentCompositeHasFillLayoutAndControlsAreCreated() {
        final RobotPopupDialog dialog = prepareDialogToTest(shellProvider.getShell());
        dialog.open();

        final Shell shell = dialog.getShell();
        assertThat(shell.getLayout()).isInstanceOf(FillLayout.class);
        assertThat(shell.getChildren()).hasSize(1);
        assertThat(shell.getChildren()[0]).isInstanceOf(Composite.class);

        final Composite internalParent = (Composite) shell.getChildren()[0];

        assertThat(internalParent.getChildren()).hasSize(1);
        assertThat(internalParent.getChildren()[0]).isInstanceOf(Label.class);
        assertThat(((Label) internalParent.getChildren()[0]).getText()).isEqualTo("test label");

        assertThat(((Label) internalParent.getChildren()[0])).isSameAs(dialog.getFocusControl());
    }

    @SuppressWarnings("restriction")
    private static RobotPopupDialog prepareDialogToTest(final Shell shell) {
        return new RobotPopupDialog(shell, mock(IThemeEngine.class)) {
            private Label label;

            @Override
            protected Control createDialogControls(final Composite parent) {
                final Composite composite = new Composite(parent, SWT.NONE);

                label = new Label(composite, SWT.NONE);
                label.setText("test label");
                return composite;
            }

            @Override
            protected Control getFocusControl() {
                return label;
            }
        };
    }

}
