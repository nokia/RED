/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robotframework.red.junit.ShellProvider;

/**
 * @author wypych
 */
public class CellEditorValueValidationListenerTest {

    @Rule
    public ShellProvider shell = new ShellProvider();

    @Mock
    private Control control;

    private CellEditorValueValidationJobScheduler<Object> validationJobScheduler;

    private CellEditorValueValidationJobScheduler<Object>.CellEditorValueValidationListener cellValidationListener;

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
        final boolean canCloseCellEditor = validationJobScheduler.canCloseCellEditor();
        assertThat(canCloseCellEditor).isFalse();
        final ControlDecoration decoration = validationJobScheduler.getDecoration();
        assertThat(decoration).isNotNull();
        assertThat(decoration.getDescriptionText()).isEqualTo(errorMessage);
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

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        final Composite composite = mock(Composite.class);
        when(composite.toDisplay(any(Point.class))).thenReturn(new Point(0, 0));
        when(composite.toDisplay(anyInt(), anyInt())).thenReturn(new Point(0, 0));
        when(control.getShell()).thenReturn(shell.getShell());
        when(control.getParent()).thenReturn(composite);
        when(composite.toDisplay(any(Point.class))).thenReturn(new Point(0, 0));
        when(control.getBounds()).thenReturn(new Rectangle(0, 0, 0, 0));
        when(control.toControl(any(Point.class))).thenReturn(new Point(0, 0));
        this.validationJobScheduler = new CellEditorValueValidationJobScheduler<>(null);
        this.cellValidationListener = validationJobScheduler.new CellEditorValueValidationListener(control);
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
