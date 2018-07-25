/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.CreateFreshSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.SetSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

import com.google.common.collect.ImmutableBiMap;

public class GeneralSettingsColumnsPropertyAccessor implements IColumnPropertyAccessor<Entry<String, RobotElement>> {

    private static ImmutableBiMap<Integer, String> properties = ImmutableBiMap.of(0, "name", 1, "value");

    private RobotSettingsSection section;

    private final RobotEditorCommandsStack commandsStack;

    private int numberOfColumns;

    public GeneralSettingsColumnsPropertyAccessor(final RobotSettingsSection section,
            final RobotEditorCommandsStack commandsStack, final int numberOfColumns) {
        this.section = section;
        this.commandsStack = commandsStack;
        this.numberOfColumns = numberOfColumns;
    }

    @Override
    public Object getDataValue(Entry<String, RobotElement> rowObject, int columnIndex) {
        if (rowObject != null) {
            final RobotElement robotElement = rowObject.getValue();
            RobotSetting setting = null;
            List<String> arguments = new ArrayList<>();
            if (robotElement != null) {
                setting = (RobotSetting) robotElement;
                arguments = setting.getArguments();
            }
            if (columnIndex == 0) {
                return rowObject.getKey();
            } else if (columnIndex == numberOfColumns - 1) {
                return setting != null ? setting.getComment() : "";
            } else if (columnIndex <= arguments.size()) {
                return arguments.get(columnIndex - 1);
            }
        }
        return "";
    }

    @Override
    public void setDataValue(Entry<String, RobotElement> rowObject, int columnIndex, Object newValue) {
        if (rowObject != null) {
            final String newStringValue = newValue != null ? (String) newValue : "";
            final RobotElement robotElement = rowObject.getValue();
            if (robotElement != null) {
                final RobotSetting setting = (RobotSetting) robotElement;
                if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                    commandsStack.execute(new SetSettingArgumentCommand(setting, columnIndex - 1, newStringValue));
                } else if (columnIndex == (numberOfColumns - 1)) {
                    commandsStack.execute(new SetKeywordCallCommentCommand(setting, newStringValue));
                }
            } else if (columnIndex > 0 && newValue != null && section != null) {
                if (columnIndex == (numberOfColumns - 1)) {
                    commandsStack.execute(
                            new CreateFreshSettingCommand(section, rowObject.getKey(), new ArrayList<>(),
                                    newStringValue));
                } else {
                    final List<String> args = newArrayList(Collections.nCopies(columnIndex - 1, "\\"));
                    args.add((String) newValue);
                    commandsStack.execute(new CreateFreshSettingCommand(section, rowObject.getKey(), args));
                }
            }
        }
    }

    @Override
    public int getColumnCount() {
        return numberOfColumns;
    }

    @Override
    public String getColumnProperty(int columnIndex) {
        return properties.get(columnIndex);
    }

    @Override
    public int getColumnIndex(String propertyName) {
        return properties.inverse().get(propertyName);
    }

    public void setNumberOfColumns(int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
    }

    public void setSection(RobotSettingsSection section) {
        this.section = section;
    }

}
