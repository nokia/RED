package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * @author mmarzec
 *
 */
public class VariablesViewerColumnSelectionListener extends SelectionAdapter {

    private TableViewer viewer;

    private VariablesViewerComparator comparator;

    private int columnIndex;

    public VariablesViewerColumnSelectionListener(final TableViewer viewer, final VariablesViewerComparator comparator,
            final int columnIndex) {
        this.viewer = viewer;
        this.comparator = comparator;
        this.columnIndex = columnIndex;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (viewer.getComparator() == null) {
            viewer.setComparator(comparator);
        }
        comparator.setColumn(columnIndex);
        int direction = comparator.getDirection();
        viewer.getTable().setSortDirection(direction);
        if (direction == SWT.NONE) {
            viewer.getTable().setSortColumn(null);
            viewer.setComparator(null);
        } else {
            viewer.getTable().setSortColumn(viewer.getTable().getColumn(columnIndex));
        }

        viewer.refresh();
    }
}
