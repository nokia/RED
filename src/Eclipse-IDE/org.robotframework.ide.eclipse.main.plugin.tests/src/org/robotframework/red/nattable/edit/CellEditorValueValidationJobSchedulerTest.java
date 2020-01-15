/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.nattable.edit.CellEditorValueValidator.CellEditorValueValidationException;

@ExtendWith(FreshShellExtension.class)
public class CellEditorValueValidationJobSchedulerTest {

    @FreshShell
    Shell shell;

    private CellEditorValueValidator<Object> validator;

    private CellEditorValueValidationJobScheduler<Object> validationJobScheduler;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {
        validator = mock(CellEditorValueValidator.class);
        validationJobScheduler = new CellEditorValueValidationJobScheduler<>(validator);
    }

    @Test
    public void validationJobIsCancelled_whenEditorCanBeClosed() throws Exception {
        final Text text = new Text(shell, SWT.SINGLE);
        validationJobScheduler.armRevalidationOn(text, 1);
        text.setText("x");

        assertThat(validationJobScheduler.canCloseCellEditor()).isTrue();
        assertThat(validationJobScheduler.getValidationJob().getResult()).isNull();
    }

    @Test
    public void validationJobIsNotCancelled_whenEditorCanNotBeClosed() throws Exception {
        doThrow(CellEditorValueValidationException.class).when(validator).validate(any(), anyInt());

        final Text text = new Text(shell, SWT.SINGLE);
        validationJobScheduler.armRevalidationOn(text, 1);
        text.setText("x");

        assertThat(validationJobScheduler.canCloseCellEditor()).isFalse();
        assertThat(validationJobScheduler.getValidationJob().getResult()).isNotNull();
    }
}
