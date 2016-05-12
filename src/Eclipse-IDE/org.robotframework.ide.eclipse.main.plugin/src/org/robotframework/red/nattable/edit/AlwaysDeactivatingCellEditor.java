/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IEditErrorHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.robotframework.red.nattable.NewElementsCreator;

/**
 * @author Michal Anglart
 *
 */
public class AlwaysDeactivatingCellEditor extends AbstractCellEditor {

    private final NewElementsCreator<?> creator;

    public <T> AlwaysDeactivatingCellEditor(final NewElementsCreator<T> creator) {
        this.creator = creator;
    }

    @Override
    public Object getCanonicalValue(final IEditErrorHandler conversionErrorHandler) {
        return getEditorValue();
    }

    @Override
    public Object getEditorValue() {
        return creator.createNew();
    }

    @Override
    public void setEditorValue(final Object value) {
        // no editor, so nothing to do
    }

    @Override
    public Control getEditorControl() {
        return null;
    }

    @Override
    public Control createEditorControl(final Composite parent) {
        return null;
    }

    @Override
    protected Control activateCell(final Composite parent, final Object originalCanonicalValue) {
        commit(MoveDirectionEnum.UP);
        return null;
    }
}
