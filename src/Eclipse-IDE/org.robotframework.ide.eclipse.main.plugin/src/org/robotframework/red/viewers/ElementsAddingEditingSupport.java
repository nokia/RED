/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import org.eclipse.jface.viewers.AlwaysDeactivatingCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 *
 */
public abstract class ElementsAddingEditingSupport extends EditingSupport {

    protected final int index;

    protected final NewElementsCreator<?> creator;

    public ElementsAddingEditingSupport(final ColumnViewer viewer, final int index,
            final NewElementsCreator<?> creator) {
        super(viewer);
        this.index = index;
        this.creator = creator;
    }

    @Override
    protected boolean canEdit(final Object element) {
        return true;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof ElementAddingToken) {
            return new AlwaysDeactivatingCellEditor((Composite) getViewer().getControl());
        }
        return null;
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof ElementAddingToken) {
            scheduleViewerRefreshAndEditorActivation(creator.createNew(((ElementAddingToken) element).getParent()));
        }
    }

    protected int getColumnShift() {
        return 0;
    }

    // refresh and cell editor activation has to be done in GUI thread but after
    // current cell editor was properly deactivated
    protected final void scheduleViewerRefreshAndEditorActivation(final Object value) {
        final Display display = getViewer().getControl().getDisplay();
        display.asyncExec(refreshAndEdit(value));
    }

    @VisibleForTesting
    Runnable refreshAndEdit(final Object value) {
        return new Runnable() {
            @Override
            public void run() {
                final ColumnViewer viewer = getViewer();
                if (viewer.getControl() != null && viewer.getControl().isDisposed()) {
                    return;
                }
                viewer.refresh();
                if (value != null) {
                    viewer.editElement(value, index + getColumnShift());
                }
            }
        };
    }

    public abstract static class NewElementsCreator<T> {

        public T createNew() {
            return null;
        }

        public T createNew(@SuppressWarnings("unused") final Object parent) {
            return createNew();
        }
    }
}
