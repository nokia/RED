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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.nattable.edit.CellEditorValueValidator.CellEditorValueValidationException;

@RunWith(MockitoJUnitRunner.class)
public class CellEditorValueValidationJobSchedulerTest {

    @Rule
    public ShellProvider shell = new ShellProvider();

    @Mock
    private CellEditorValueValidator<Object> validator;

    private CellEditorValueValidationJobScheduler<Object> validationJobScheduler;

    @Before
    public void setUp() {
        validationJobScheduler = new CellEditorValueValidationJobScheduler<>(validator);
    }

    @Test
    public void validationJobIsCancelled_whenEditorCanBeClosed() throws Exception {
        final Text text = new Text(shell.getShell(), SWT.SINGLE);
        validationJobScheduler.armRevalidationOn(text, 1);
        text.setText("x");

        assertThat(validationJobScheduler.canCloseCellEditor()).isTrue();
        assertThat(validationJobScheduler.getValidationJob().getResult()).isNull();
    }

    @Test
    public void validationJobIsNotCancelled_whenEditorCanNotBeClosed() throws Exception {
        doThrow(CellEditorValueValidationException.class).when(validator).validate(any(), anyInt());

        final Text text = new Text(shell.getShell(), SWT.SINGLE);
        validationJobScheduler.armRevalidationOn(text, 1);
        text.setText("x");

        assertThat(validationJobScheduler.canCloseCellEditor()).isFalse();
        assertThat(validationJobScheduler.getValidationJob().getResult()).isNotNull();
    }
}
