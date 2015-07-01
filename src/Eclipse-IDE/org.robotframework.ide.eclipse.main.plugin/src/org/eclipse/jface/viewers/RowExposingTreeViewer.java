package org.eclipse.jface.viewers;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;

public class RowExposingTreeViewer extends TreeViewer {

    public RowExposingTreeViewer(final Composite parent, final int style) {
        super(parent, style);
    }

    @Override
    public ViewerRow getViewerRowFromItem(final Widget item) { // changing protected to public
        return super.getViewerRowFromItem(item);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getSelectionFromWidget() {
        return super.getSelectionFromWidget();
    }

    /**
     * Disposes all columns starting from given index
     */
    public void removeColumns(final int beginIndex) {
        int i = 0;
        for (final TreeColumn column : getTree().getColumns()) {
            if (i >= beginIndex) {
                column.dispose();
            }
            i++;
        }
    }

    /**
     * Disposes all columns
     */
    public void removeAllColumns() {
        removeColumns(0);
    }

    public void packFirstColumn() {
        getTree().getColumn(0).pack();
    }

    public boolean hasAtLeastOneColumn() {
        return getTree().getColumnCount() > 0;
    }
}