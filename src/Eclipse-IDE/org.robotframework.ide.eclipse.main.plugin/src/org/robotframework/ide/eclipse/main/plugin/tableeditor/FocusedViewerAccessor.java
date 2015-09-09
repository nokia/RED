/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;

public class FocusedViewerAccessor {

    private final ViewerColumnsManagingStrategy columnsManager;
    private final ColumnViewer viewer;

    public FocusedViewerAccessor(final ViewerColumnsManagingStrategy columnsManager, final ColumnViewer viewer) {
        this.viewer = viewer;
        this.columnsManager = columnsManager;
    }

    public ColumnViewer getViewer() {
        return viewer;
    }

    public ViewerCell getFocusedCell() {
        return getViewer().getColumnViewerEditor().getFocusCell();
    }

    public ViewerColumnsManagingStrategy getColumnsManager() {
        return columnsManager;
    }

    public static class ViewerColumnsManagingStrategy {

        @SuppressWarnings("unused")
        public void addColumn(final ColumnViewer viewer) {
            // implement in subclasses
        }

        @SuppressWarnings("unused")
        public void removeColumn(final ColumnViewer viewer) {
            // implement in subclasses
        }
    }
}