package org.eclipse.jface.viewers;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.swt.widgets.Table;

import com.google.common.primitives.Ints;

public class ColumnAddingEditingSupport extends EditingSupport {

    private int index;

    private final ColumnProviders columnsFactory;

    public ColumnAddingEditingSupport(final TableViewer column, final int index, final ColumnProviders columnsFactory) {
        super(column);
        this.index = index;
        this.columnsFactory = columnsFactory;
    }

    private Table getTable() {
        return ((TableViewer) getViewer()).getTable();
    }

    @Override
    protected boolean canEdit(final Object element) {
        return true;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        return new AlwaysDeactivatingCellEditor(getTable());
    }

    @Override
    protected Object getValue(final Object element) {
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        columnsFactory.createColumnAndMoveToSpecifiedPosition((TableViewer) getViewer(), index);

        scheduleViewerRefreshAndEditorActivation(element, getTable().getColumnOrder()[index]);
        index++;
    }

    // refresh and cell editor activation has to be done in GUI thread but after
    // current cell editor was properly deactivated
    private void scheduleViewerRefreshAndEditorActivation(final Object element, final int cellColumnToActivate) {
        getViewer().getControl().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                getViewer().refresh();
                getViewer().editElement(element, cellColumnToActivate);
            }
        });
    }

    public static abstract class ColumnProviders {

        public abstract void createColumn(int index);

        void createColumnAndMoveToSpecifiedPosition(final TableViewer viewer, final int index) {
            createColumn(index);

            final Table table = viewer.getTable();
            table.setColumnOrder(alterOrder(table.getColumnOrder(), index));
        }

        private int[] alterOrder(final int[] currentOrder, final int index) {
            // moves newly added column to desired place
            final List<Integer> ints = newArrayList(Ints.asList(currentOrder));
            final Integer removed = ints.remove(ints.size() - 1);
            ints.add(index, removed);
            return Ints.toArray(ints);
        }
    }
}
