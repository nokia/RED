/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.eclipse.jface.viewers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This is a specialized custom cell editor which deactivates just after
 * activation which allows editing support to immediately influence the
 * displayed model.
 *
 */
public class AlwaysDeactivatingCellEditor extends CellEditor {

    public AlwaysDeactivatingCellEditor(final Composite parent) {
        super(parent);
    }

    @Override
    protected Control createControl(final Composite parent) {
        return null;
    }

    @Override
    public void activate() {
        fireApplyEditorValue();
    }

    @Override
    protected Object doGetValue() {
        return null;
    }

    @Override
    protected void doSetFocus() {
        // nothing to do
    }

    @Override
    protected void doSetValue(final Object value) {
        // nothing to do
    }

}
