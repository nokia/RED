/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.SetSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.SetSettingNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

import com.google.common.collect.ImmutableBiMap;

public class ImportSettingsColumnsPropertyAccessor implements IColumnPropertyAccessor<RobotKeywordCall> {
    
    private static ImmutableBiMap<Integer, String> properties = ImmutableBiMap.of(0, "name", 1, "value");
    
    private final RobotEditorCommandsStack commandsStack;
    
    private int numberOfColumns;
    
    public ImportSettingsColumnsPropertyAccessor(final RobotEditorCommandsStack commandsStack, final int numberOfColumns) {
        this.commandsStack = commandsStack;
        this.numberOfColumns = numberOfColumns;
    }

    @Override
    public Object getDataValue(final RobotKeywordCall rowObject, final int columnIndex) {
        final List<String> arguments = rowObject.getArguments();
        if (columnIndex == 0) {
            return rowObject.getName();
        } else if (columnIndex == numberOfColumns - 1) {
            return rowObject.getComment();
        } else if (columnIndex <= arguments.size()) {
            return arguments.get(columnIndex - 1);
        }
        return "";
    }

    @Override
    public void setDataValue(final RobotKeywordCall rowObject, final int columnIndex, final Object newValue) {
        if (columnIndex == 0) {
            commandsStack.execute(new SetSettingNameCommand((RobotSetting) rowObject, (String) newValue));
        } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
            commandsStack.execute(new SetSettingArgumentCommand(rowObject, columnIndex - 1, (String) newValue));
        } else if (columnIndex == (numberOfColumns - 1)) {
            commandsStack.execute(new SetKeywordCallCommentCommand(rowObject, (String) newValue));
        }
    }

    @Override
    public int getColumnCount() {
        return numberOfColumns;
    }

    @Override
    public String getColumnProperty(final int columnIndex) {
        final String property = properties.get(columnIndex);
        return property;
    }

    @Override
    public int getColumnIndex(final String propertyName) {
        return properties.inverse().get(propertyName);
    }

    
    public void setNumberOfColumns(final int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
    }

    
}
