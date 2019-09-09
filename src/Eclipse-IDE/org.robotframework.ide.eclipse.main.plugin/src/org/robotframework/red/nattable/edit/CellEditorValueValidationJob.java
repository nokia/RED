/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.robotframework.red.nattable.edit.CellEditorValueValidator.CellEditorValueValidationException;

/**
 * @author Michal Anglart
 */
public class CellEditorValueValidationJob<V> extends Job {

    static QualifiedName getLockPropertyName() {
        return new QualifiedName("cell.editor", "lock");
    }

    static QualifiedName getErrorMessagePropertyName() {
        return new QualifiedName("cell.editor", "error");
    }

    private final CellEditorValueValidator<V> validator;

    private final V valueToValidate;

    private final int rowId;

    CellEditorValueValidationJob(final CellEditorValueValidator<V> validator, final V valueToValidate,
            final int rowId) {
        super("Validating cell input");
        this.validator = validator;
        this.valueToValidate = valueToValidate;
        this.rowId = rowId;
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        try {
            setProperty(getLockPropertyName(), Boolean.TRUE);
            validator.validate(valueToValidate, rowId);
            setProperty(getLockPropertyName(), Boolean.FALSE);
        } catch (final CellEditorValueValidationException e) {
            setProperty(getErrorMessagePropertyName(), e.getMessage());
        }
        return Status.OK_STATUS;
    }

    boolean isCellValid() {
        try {
            validator.validate(valueToValidate, rowId);
            return true;
        } catch (final CellEditorValueValidationException e) {
            return false;
        }
    }

}
