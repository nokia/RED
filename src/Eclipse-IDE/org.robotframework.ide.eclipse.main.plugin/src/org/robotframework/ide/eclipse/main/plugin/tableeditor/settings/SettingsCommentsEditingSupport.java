package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;

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
        final RobotSetting setting = getSetting(element);
        return setting != null ? setting.getComment() : "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        final RobotSetting setting = getSetting(element);

    }

    private RobotSetting getSetting(final Object element) {
        if (element instanceof RobotSetting) {
            return (RobotSetting) element;
        } else if (element instanceof Entry<?, ?>) {
            return (RobotSetting) ((Entry<?, ?>) element).getValue();
        }
        return null;
    }
}
