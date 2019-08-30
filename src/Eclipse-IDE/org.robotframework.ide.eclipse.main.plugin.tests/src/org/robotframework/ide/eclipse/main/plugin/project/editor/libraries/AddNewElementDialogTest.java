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
import java.util.stream.Stream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.swt.SwtThread;

public class AddNewElementDialogTest {

    @Rule
    public ShellProvider shell = new ShellProvider();

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
                findControls(Label.class, root).map(Label::getText).forEach(allLabelsAndInputs::add);
                findControls(StyledText.class, root).map(StyledText::getText).forEach(allLabelsAndInputs::add);

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

    private static <T extends Control> Stream<T> findControls(final Class<T> controlClass, final Control root) {
        final List<T> allControls = new ArrayList<>();
        findControl(controlClass, root, allControls);
        return allControls.stream();
    }

    private static <T extends Control> void findControl(final Class<T> controlClass, final Control root,
            final List<T> allFound) {
        if (controlClass.isInstance(root)) {
            allFound.add(controlClass.cast(root));

        } else if (root instanceof Composite) {
            final Composite comp = (Composite) root;
            for (final Control child : comp.getChildren()) {
                findControl(controlClass, child, allFound);
            }
        }
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
