package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateSettingKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetArgumentOfSettingKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

public class GeneralSettingsArgsEditingSupport extends EditingSupport {

    private final int index;
    private final RobotEditorCommandsStack commandsStack;

    public GeneralSettingsArgsEditingSupport(final ColumnViewer column, final int index,
            final RobotEditorCommandsStack commandsStack) {
        super(column);
        this.index = index;
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
        @SuppressWarnings("unchecked")
        final Entry<String, RobotElement> entry = (Entry<String, RobotElement>) element;
        final RobotSetting setting = (RobotSetting) entry.getValue();

        if (setting != null) {
            final List<String> arguments = setting.getArguments();
            if (index < arguments.size()) {
                return arguments.get(index);
            }
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        @SuppressWarnings("unchecked")
        final Entry<String, RobotElement> entry = (Entry<String, RobotElement>) element;
        final String keywordName = entry.getKey();
        final RobotSetting setting = (RobotSetting) entry.getValue();

        if (setting == null) {
            final GeneralSettingsModel model = (GeneralSettingsModel) getViewer().getInput();
            commandsStack.execute(new CreateSettingKeywordCall(model.getSection(), keywordName, (String) value));
        } else {
            commandsStack.execute(new SetArgumentOfSettingKeywordCall(setting, index, (String) value));
        }
    }

}
