package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

class GeneralSettingsCommentsEditingSupport extends EditingSupport {

    private final RobotEditorCommandsStack commandsStack;

    GeneralSettingsCommentsEditingSupport(final ColumnViewer column, final RobotEditorCommandsStack commandsStack) {
        super(column);
        this.commandsStack = commandsStack;
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
        final String comment = (String) value;
        final RobotSetting setting = getSetting(element);

        if (setting == null && !comment.isEmpty()) {
            final String keywordName = getSettingName(element);
            final GeneralSettingsModel model = (GeneralSettingsModel) getViewer().getInput();
            commandsStack.execute(new CreateSettingKeywordCallCommand(model.getSection(), keywordName,
                    new ArrayList<String>(), comment));
        } else if (setting != null) {
            commandsStack.execute(new SetKeywordCallCommentCommand(setting, comment));
        }
    }

    private RobotSetting getSetting(final Object element) {
        return (RobotSetting) ((Entry<?, ?>) element).getValue();
    }

    private String getSettingName(final Object element) {
        return (String) ((Entry<?, ?>) element).getKey();
    }
}
