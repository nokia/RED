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


/**
 * @author Michal Anglart
 *
 */
public abstract class ElementsAddingEditingSupport extends EditingSupport {

    protected final int index;

    private final NewElementsCreator<?> creator;

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
            final int indexToActivate = index + getColumnShift();
            scheduleViewerRefreshAndEditorActivation(creator.createNew(((ElementAddingToken) element).getParent()),
                    indexToActivate);
        }
    }

    protected int getColumnShift() {
        return 0;
    }

    // refresh and cell editor activation has to be done in GUI thread but after
    // current cell editor was properly deactivated
    private void scheduleViewerRefreshAndEditorActivation(final Object value, final int cellColumnToActivate) {
        getViewer().getControl().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                getViewer().refresh();
                if (value != null) {
                    getViewer().editElement(value, cellColumnToActivate);
                }
            }
        });
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
