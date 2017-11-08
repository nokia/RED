/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 */
public class CellEditorValueValidationJobScheduler<V> {

    private final CellEditorValueValidator<V> validator;

    private CellEditorValueValidationJob<V> validationJob;

    private ControlDecoration decoration;

    private Color originalForeground;

    private boolean isClosingLocked = false;

    CellEditorValueValidationJobScheduler(final CellEditorValueValidator<V> validator) {
        this.validator = validator;
    }

    void rescheduleValidation(final V value, final Control control) {
        isClosingLocked = true;
        if (validationJob != null && validationJob.getState() == Job.SLEEPING) {
            validationJob.cancel();
        }
        validationJob = new CellEditorValueValidationJob<>(validator, value);
        validationJob.addJobChangeListener(new CellEditorValueValidationListener(control));
        validationJob.schedule(300);
    }

    boolean canCloseCellEditor() {
        waitForJobToFinish();
        return !isClosingLocked;
    }

    private void waitForJobToFinish() {
        final Job job = validationJob;
        if (job != null) {
            try {
                job.join();
            } catch (final InterruptedException e) {
                throw new IllegalStateException("Cell editor validation job has been interrupted!", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void armRevalidationOn(final Text textControl) {
        if (validator == null || textControl == null || textControl.isDisposed()) {
            return;
        }
        originalForeground = textControl.getForeground();
        textControl.addModifyListener(e -> rescheduleValidation((V) textControl.getText(), textControl));
    }

    @VisibleForTesting
    public CellEditorValueValidator<V> getValidator() {
        return this.validator;
    }

    @VisibleForTesting
    class CellEditorValueValidationListener extends JobChangeAdapter {

        private final Control control;

        public CellEditorValueValidationListener(final Control control) {
            this.control = control;
        }

        @Override
        public void done(final IJobChangeEvent event) {
            if (event.getJob().getResult() == null) {
                return;
            }
            final Object prop = event.getJob().getProperty(CellEditorValueValidationJob.getLockPropertyName());
            if (prop instanceof Boolean) {
                isClosingLocked = (Boolean) prop;
            }
            final String errorMsg = (String) event.getJob()
                    .getProperty(CellEditorValueValidationJob.getErrorMessagePropertyName());

            SwtThread.syncExec(() -> {
                if (decoration != null) {
                    decoration.hide();
                    decoration.dispose();
                }
            });
            decoration = SwtThread.syncEval(Evaluation.of(() -> {
                if (control == null || control.isDisposed()) {
                    return null;
                }
                if (errorMsg == null) {
                    control.setForeground(originalForeground);
                    return null;
                }
                control.setForeground(ColorsManager.getColor(255, 0, 0));
                final ControlDecoration dec = new ControlDecoration(control, SWT.LEFT | SWT.TOP);
                dec.setDescriptionText(errorMsg);
                dec.setImage(FieldDecorationRegistry.getDefault()
                        .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
                        .getImage());
                return dec;
            }));

        }
    }

    @VisibleForTesting
    ControlDecoration getDecoration() {
        return this.decoration;
    }
}
