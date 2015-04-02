package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.robotframework.ide.eclipse.main.plugin.tempmodel.Keyword;

public class TableEditingSupport extends EditingSupport {

    private final TableViewer viewer;

    private final CellEditor editor;

    private boolean isColumnAdded;

    private boolean isViewerRefreshed;

    private final int colNum;

    public TableEditingSupport(final TableViewer v, final int col) {
        super(v);
        this.viewer = v;
        this.editor = new TextCellEditor(viewer.getTable());
        this.colNum = col;

        editor.addListener(new ICellEditorListener() {

            @Override
            public void editorValueChanged(final boolean oldValidState, final boolean newValidState) {
                final Table table = viewer.getTable();
				final int argsNumber = ((Keyword) table.getItem(
						table.getSelectionIndex()).getData()).getArguments()
						.size();
                final int colCount = table.getColumnCount();
                if (colCount - 1 < argsNumber && colNum == colCount && !isColumnAdded) {
                    final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
                    final TableColumn column = viewerColumn.getColumn();
                    column.setWidth(100);
                    column.setResizable(true);
                    viewerColumn.setEditingSupport(new TableEditingSupport(viewer, colCount + 1));
                    viewerColumn.setLabelProvider(new ColumnLabelProvider() {

                        @Override
                        public String getText(final Object element) {
                            final Keyword k = (Keyword) element;
                            return "new column";
                        }
                    });
                    isColumnAdded = true;
                }
            }

            @Override
            public void cancelEditor() {
            }

            @Override
            public void applyEditorValue() {
                if (!isViewerRefreshed) {
                    viewer.refresh();
                    isViewerRefreshed = true;
                }
            }
        });

    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        return editor;
    }

    @Override
    protected boolean canEdit(final Object element) {
        final Keyword k = (Keyword) element;
		return k.getArguments().size() + 1 >= colNum;
    }

    @Override
    protected Object getValue(final Object element) {
        final Keyword k = (Keyword) element;
        if (colNum == 1) {
            return k.getName();
        } else if (colNum == 2) {
			return k.getArguments().get(0);
        } else {
			return k.getArguments().get(1);
        }
    }

    @Override
    protected void setValue(final Object element, final Object userInputValue) {
        final Keyword k = (Keyword) element;
        if (colNum == 1) {
        } else if (colNum == 2) {
        } else {
        }

        viewer.update(element, null);
    }
}
