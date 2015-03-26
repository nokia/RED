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

public class TableEditingSupport extends EditingSupport {

    private final TableViewer viewer;

    private final CellEditor editor;

    private boolean isColumnAdded;

    private boolean isViewerRefreshed;

    private final int colNum;

    public TableEditingSupport(TableViewer v, int col) {
        super(v);
        this.viewer = v;
        this.editor = new TextCellEditor(viewer.getTable());
        this.colNum = col;

        editor.addListener(new ICellEditorListener() {

            @Override
            public void editorValueChanged(boolean oldValidState, boolean newValidState) {
                Table table = viewer.getTable();
                int argsNumber = ((Keyword) table.getItem(table.getSelectionIndex()).getData()).getArgsNumber();
                int colCount = table.getColumnCount();
                if (colCount - 1 < argsNumber && colNum == colCount && !isColumnAdded) {
                    final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
                    TableColumn column = viewerColumn.getColumn();
                    column.setWidth(100);
                    column.setResizable(true);
                    viewerColumn.setEditingSupport(new TableEditingSupport(viewer, colCount + 1));
                    viewerColumn.setLabelProvider(new ColumnLabelProvider() {

                        @Override
                        public String getText(Object element) {
                            Keyword k = (Keyword) element;
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
    protected CellEditor getCellEditor(Object element) {
        return editor;
    }

    @Override
    protected boolean canEdit(Object element) {
        Keyword k = (Keyword) element;
        return k.getArgsNumber() + 1 >= colNum;
    }

    @Override
    protected Object getValue(Object element) {
        Keyword k = (Keyword) element;
        if (colNum == 1) {
            return k.getName();
        } else if (colNum == 2) {
            return k.getArg1();
        } else {
            return k.getArg2();
        }
    }

    @Override
    protected void setValue(Object element, Object userInputValue) {
        Keyword k = (Keyword) element;
        if (colNum == 1) {
            k.setName(String.valueOf(userInputValue));
        } else if (colNum == 2) {
            k.setArg1(String.valueOf(userInputValue));
        } else {
            k.setArg2(String.valueOf(userInputValue));
        }

        viewer.update(element, null);
    }
}
