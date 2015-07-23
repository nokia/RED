package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

class SettingsCommentsEditingSupport extends RobotElementEditingSupport {

    SettingsCommentsEditingSupport(final ColumnViewer column, final RobotEditorCommandsStack commandsStack,
            final NewElementsCreator creator) {
        super(column, ((TableViewer) column).getTable().getColumnCount(), commandsStack, creator);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof RobotSetting) {
            return new TextCellEditor(((TableViewer) getViewer()).getTable());
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RobotSetting) {
            final RobotSetting setting = (RobotSetting) element;
            return setting != null ? setting.getComment() : "";
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotSetting) {
            final RobotSetting setting = (RobotSetting) element;
            final String comment = (String) value;

            commandsStack.execute(new SetKeywordCallCommentCommand(setting, comment));
        } else {
            super.setValue(element, value);
        }
    }
}
