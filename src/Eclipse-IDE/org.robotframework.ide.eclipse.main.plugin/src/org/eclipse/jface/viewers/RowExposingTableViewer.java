package org.eclipse.jface.viewers;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;

public class RowExposingTableViewer extends TableViewer {

    public RowExposingTableViewer(final Composite parent, final int style) {
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
        for (final TableColumn column : getTable().getColumns()) {
            if (i >= beginIndex) {
                column.dispose();
            }
            i++;
        }
    }

    public void packFirstColumn() {
        getTable().getColumn(0).pack();
    }

    public boolean hasAtLeastOneColumn() {
        return getTable().getColumnCount() > 0;
    }
}