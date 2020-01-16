/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.swt.SwtThread;

@ExtendWith(FreshShellExtension.class)
public class VariableMappingDialogTest {

    @FreshShell
    Shell shell;

    @Test
    public void entryDialogProperlyGeneratesVariableMapping_whenNameAndValueAreProvidedInTextBoxes() throws Exception {
        final AtomicBoolean finished = new AtomicBoolean(false);
        final AtomicReference<VariableMappingDialog> dialog = new AtomicReference<>(null);

        final Thread guiChangesRequestingThread = new Thread(() -> {
            SwtThread.asyncExec(() -> {
                dialog.set(new VariableMappingDialog(shell));
                dialog.get().open();
                finished.set(true);
            });
            SwtThread.asyncExec(() -> {
                dialog.get().getNameText().setText("${someVar}");
                dialog.get().getValueText().setText("123456");
                dialog.get().getOkButton().notifyListeners(SWT.Selection, new Event());
            });
        });
        guiChangesRequestingThread.start();
        guiChangesRequestingThread.join();

        while (!finished.get()) {
            Thread.sleep(100);
            while (shell.getDisplay().readAndDispatch()) {
                // handle all events coming to UI
            }
        }

        assertThat(dialog.get().getMapping()).isEqualTo(VariableMapping.create("${someVar}", "123456"));
    }
}
