/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetSettingKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

import com.google.common.collect.ImmutableBiMap;

public class MetadataSettingsColumnsPropertyAccessor implements IColumnPropertyAccessor<RobotKeywordCall> {
    
    private static ImmutableBiMap<Integer, String> properties = ImmutableBiMap.of(0, "name", 1, "value", 2, "comment");
    
    private final RobotEditorCommandsStack commandsStack;

    public MetadataSettingsColumnsPropertyAccessor(final RobotEditorCommandsStack commandsStack) {
        this.commandsStack = commandsStack;
    }

    @Override
    public Object getDataValue(RobotKeywordCall rowObject, int columnIndex) {
        final List<String> arguments = rowObject.getArguments();
        if (columnIndex == 0) {
            return arguments.isEmpty() ? "" : arguments.get(0);
        } else if (columnIndex == 1) {
            return arguments.size() > 1 ? arguments.get(1) : "";
        } else if (columnIndex == 2) {
            return rowObject.getComment();
        }
        throw new IllegalStateException("Unknown column with " + columnIndex + " index");
    }

    @Override
    public void setDataValue(RobotKeywordCall rowObject, int columnIndex, Object newValue) {
        final String newStringValue = newValue != null ? (String) newValue : "";
        if (columnIndex < 2) {
            commandsStack.execute(new SetSettingKeywordCallArgumentCommand(rowObject, columnIndex, newStringValue));
        } else if (columnIndex == 2) {
            commandsStack.execute(new SetKeywordCallCommentCommand(rowObject, newStringValue));
        }
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnProperty(int columnIndex) {
        return properties.get(columnIndex);
    }

    @Override
    public int getColumnIndex(String propertyName) {
        return properties.inverse().get(propertyName);
    }

}
