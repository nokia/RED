package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

class GeneralSettingsArgsEditingSupport extends EditingSupport {

    private final int index;
    private final RobotEditorCommandsStack commandsStack;

    GeneralSettingsArgsEditingSupport(final ColumnViewer column, final int index,
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
        final RobotSetting setting = getSetting(element);
        if (setting == null) {
            return "";
        }
        final List<String> arguments = setting.getArguments();
        return index < arguments.size() ? arguments.get(index) : "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        final String arg = (String) value;
        final RobotSetting setting = getSetting(element);

        if (setting == null && !arg.isEmpty()) {
            final String keywordName = getSettingName(element);
            final GeneralSettingsModel model = (GeneralSettingsModel) getViewer().getInput();
            final List<String> args = newArrayList(Collections.nCopies(index, ""));
            args.add(arg);
            commandsStack.execute(new CreateSettingKeywordCallCommand(model.getSection(), keywordName, args));
        } else if (setting != null) {
            commandsStack.execute(new SetKeywordCallArgumentCommand(setting, index, arg));
        }
    }

    private RobotSetting getSetting(final Object element) {
        return (RobotSetting) ((Entry<?, ?>) element).getValue();
    }

    private String getSettingName(final Object element) {
        return (String) ((Entry<?, ?>) element).getKey();
    }
}
