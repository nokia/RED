package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public class SettingsCommentsEditingSupport extends EditingSupport {

    public SettingsCommentsEditingSupport(final ColumnViewer column) {
        super(column);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        return new TextCellEditor(((TableViewer) getViewer()).getTable());
    }

    @Override
    protected boolean canEdit(final Object element) {
        return true;
    }

    @Override
    protected Object getValue(final Object element) {
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        // TODO Auto-generated method stub

    }

}
