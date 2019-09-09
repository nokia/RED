/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.robotframework.red.junit.ShellProvider;

/**
 * @author wypych
 */
@RunWith(MockitoJUnitRunner.class)
public class CellEditorValueValidationListenerTest {

    @Rule
    public ShellProvider shell = new ShellProvider();

    @Mock
    private Control control;

    private CellEditorValueValidationJobScheduler<Object> validationJobScheduler;

    private CellEditorValueValidationJobScheduler<Object>.CellEditorValueValidationListener cellValidationListener;

    @Before
    public void setUp() {
        final Composite composite = mock(Composite.class);
        when(composite.toDisplay(anyInt(), anyInt())).thenReturn(new Point(0, 0));
        when(control.getShell()).thenReturn(shell.getShell());
        when(control.getParent()).thenReturn(composite);
        when(control.getBounds()).thenReturn(new Rectangle(0, 0, 0, 0));
        validationJobScheduler = new CellEditorValueValidationJobScheduler<>(null);
        cellValidationListener = validationJobScheduler.new CellEditorValueValidationListener(control);
    }

    @Test
    public void givenIJobChangeEventWithStatusSuccess_andJob_withLockAndErrorMsgBothProperties_whenCallMethodNotifyAboutJobChangeState_thenShouldBeNormalExecution()
            throws InterruptedException {
        // given
        final String errorMessage = "an error";

        final IJobChangeEvent jobChangeEvent = mock(IJobChangeEvent.class);
        final Job b = new MockJob("my mock job");
        b.setProperty(MockJob.getLockPropertyName(), Boolean.TRUE);
        b.setProperty(MockJob.getErrorMessagePropertyName(), errorMessage);
        b.schedule();
        b.join();

        when(jobChangeEvent.getJob()).thenReturn(b);

        // when
        cellValidationListener.done(jobChangeEvent);

        // then
        assertThat(validationJobScheduler.canCloseCellEditor()).isFalse();
        assertThat(validationJobScheduler.getDecoration()).isNotNull()
                .extracting(ControlDecoration::getDescriptionText)
                .isEqualTo(errorMessage);
    }

    @Test
    public void givenIJobChangeEventWithStatusSuccess_andJobWithoutBothProperties_whenCallMethodNotifyAboutJobChangeState_thenShouldBeNormalExecution()
            throws InterruptedException {
        // given
        final IJobChangeEvent jobChangeEvent = mock(IJobChangeEvent.class);
        final Job b = new MockJob("my mock job");
        b.schedule();
        b.join();

        when(jobChangeEvent.getJob()).thenReturn(b);

        // when
        cellValidationListener.done(jobChangeEvent);

        // then
        assertThat(validationJobScheduler.canCloseCellEditor()).isTrue();
        assertThat(validationJobScheduler.getDecoration()).isNull();
    }

    private static class MockJob extends Job {

        public MockJob(final String name) {
            super(name);
        }

        static QualifiedName getLockPropertyName() {
            return new QualifiedName("cell.editor", "lock");
        }

        static QualifiedName getErrorMessagePropertyName() {
            return new QualifiedName("cell.editor", "error");
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            return Status.OK_STATUS;
        }
    }
}
