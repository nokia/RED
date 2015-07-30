package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;

public class FocusedViewerAccessor {

    private final ColumnViewer[] viewers;

    public FocusedViewerAccessor(final ColumnViewer... viewers) {
        this.viewers = viewers;
    }

    public ColumnViewer getViewer() {
        if (viewers.length == 0) {
            return null;
        }
        for (final ColumnViewer viewer : viewers) {
            if (viewer.getControl().isFocusControl()) {
                return viewer;
            }
        }
        return viewers[0];
    }

    public ViewerCell getFocusedCell() {
        return getViewer().getColumnViewerEditor().getFocusCell();
    }
}