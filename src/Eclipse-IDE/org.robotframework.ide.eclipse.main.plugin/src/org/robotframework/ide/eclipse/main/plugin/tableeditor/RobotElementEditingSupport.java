/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.viewers.AlwaysDeactivatingCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

public abstract class RobotElementEditingSupport extends EditingSupport {

    // Id of context which should be activated when cell editor is activated
    protected static final String DETAILS_EDITING_CONTEXT_ID = "org.robotframework.ide.eclipse.details.context";

    protected final RobotEditorCommandsStack commandsStack;

    protected final int index;

    private final NewElementsCreator creator;

    public RobotElementEditingSupport(final ColumnViewer viewer, final int index,
            final RobotEditorCommandsStack commandsStack, final NewElementsCreator creator) {
        super(viewer);
        this.commandsStack = commandsStack;
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
    private void scheduleViewerRefreshAndEditorActivation(final RobotElement value, final int cellColumnToActivate) {
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

    public abstract static class NewElementsCreator {

        public RobotElement createNew() {
            return null;
        }

        public RobotElement createNew(@SuppressWarnings("unused") final Object parent) {
            return createNew();
        }
    }
}