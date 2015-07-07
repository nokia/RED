package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;

public class FocusedViewerAccessor {

    private final ColumnViewer viewer;

    public FocusedViewerAccessor(final ColumnViewer viewer) {
        this.viewer = viewer;
    }

    public ColumnViewer getViewer() {
        return viewer;
    }

    public ViewerCell getFocusedCell() {
        return viewer.getColumnViewerEditor().getFocusCell();
    }
}