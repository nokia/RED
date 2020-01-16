/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.junit.Controls;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.swt.SwtThread;

@ExtendWith(FreshShellExtension.class)
public class AddNewElementDialogTest {

    @FreshShell
    Shell shell;

    @Test
    public void theDialogHaveProperlyCreatedAndFilledControls() throws Exception {
        final AtomicBoolean finished = new AtomicBoolean(false);
        final AtomicReference<AddNewElementDialogMock> dialog = new AtomicReference<>(null);
        final List<String> allLabelsAndInputs = new ArrayList<>();

        final Thread guiChangesRequestingThread = new Thread(() -> {
            SwtThread.asyncExec(() -> {
                dialog.set(new AddNewElementDialogMock(shell.getShell()));
                dialog.get().open();
                finished.set(true);
            });
            SwtThread.asyncExec(() -> {
                final Shell root = dialog.get().getShell();

                allLabelsAndInputs.add(root.getText());
                Controls.getControlsStream(root, Label.class).map(Label::getText).forEach(allLabelsAndInputs::add);
                Controls.getControlsStream(root, StyledText.class)
                        .map(StyledText::getText)
                        .forEach(allLabelsAndInputs::add);

                dialog.get().getOkButton().notifyListeners(SWT.Selection, new Event());
            });
        });
        guiChangesRequestingThread.start();
        guiChangesRequestingThread.join();

        while (!finished.get()) {
            Thread.sleep(100);
            while (shell.getShell().getDisplay().readAndDispatch()) {
                // handle all events coming to UI
            }
        }
        assertThat(allLabelsAndInputs).containsExactly("title", "info", "text", "", "default");
        assertThat(dialog.get().getCreatedElement()).isEqualTo("new elem");
    }

    private static class AddNewElementDialogMock extends AddNewElementDialog<String> {

        public AddNewElementDialogMock(final Shell shell) {
            super(shell);
        }

        @Override
        protected String getDialogTitle() {
            return "title";
        }

        @Override
        protected String getInfoText() {
            return "info";
        }

        @Override
        protected String getTextLabel() {
            return "text";
        }

        @Override
        protected String getDefaultText() {
            return "default";
        }

        @Override
        protected void validate(final String text, final LocalValidationCallback callback) {
            callback.passed();
        }

        @Override
        protected String createElement(final String text) {
            return "new elem";
        }
    }
}
