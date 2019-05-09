/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Optional;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertNewCellCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.RemoveStepCellCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.ReplaceWithCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.ReplaceWithEmptyCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.ReplaceWithSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetDocumentationSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.UpdateStepCellCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class KeywordCallsTableValuesChangingCommandsCollector {

    public Optional<? extends EditorCommand> collectForUpdate(final RobotKeywordCall call, final String value,
            final int column) {

        if (value == null) {
            return collectForRemoval(call, newArrayList(column));
        } else if (column < 0) {
            return Optional.empty();
        } else if (column > 0 && call.isDocumentationSetting()) {
            return Optional.of(new SetDocumentationSettingCommand(call, value));
        }

        final List<String> data = ExecutablesRowView.rowData(call);
        if (data.size() == 1 && data.get(0).isEmpty() && column > 0) {
            data.set(0, "\\");
        }
        final int repeat = column - data.size() + 1;
        for (int i = 0; i < repeat; i++) {
            data.add("\\");
        }
        data.set(column, value);

        final int index = call.getIndex();
        if (call.isExecutable() && looksLikeSetting(data)) {
            return Optional.of(ReplaceWithSettingCommand.replaceCall(call, index, data));

        } else if (call.isExecutable() && looksLikeEmpty(data)) {
            return Optional.of(ReplaceWithEmptyCommand.replaceCall(call, index, data));

        } else if (call.isExecutable()) {
            return Optional.of(new UpdateStepCellCommand(call, value, column));

        } else if (call.isLocalSetting() && looksLikeCall(data)) {
            return Optional.of(ReplaceWithCallCommand.replaceSetting(call, index, data));

        } else if (call.isLocalSetting() && looksLikeEmpty(data)) {
            return Optional.of(ReplaceWithEmptyCommand.replaceSetting(call, index, data));

        } else if (call.isLocalSetting()) {
            return Optional.of(new UpdateStepCellCommand(call, value, column));

        } else if (call.isEmptyLine() && looksLikeCall(data)) {
            return Optional.of(ReplaceWithCallCommand.replaceEmpty(call, index, data));

        } else if (call.isEmptyLine() && looksLikeSetting(data)) {
            return Optional.of(ReplaceWithSettingCommand.replaceEmpty(call, index, data));

        } else if (call.isEmptyLine()) {
            return Optional.of(new UpdateStepCellCommand(call, value, column));
        }
        return Optional.empty();
    }

    public Optional<? extends EditorCommand> collectForInsertion(final RobotKeywordCall call, final int column) {
        final List<String> data = ExecutablesRowView.rowData(call);

        if (column < 0 || column > data.size()) {
            return Optional.empty();
        }
        data.add(column, "");

        final int index = call.getIndex();
        if (call.isExecutable() && looksLikeSetting(data)) {
            return Optional.of(ReplaceWithSettingCommand.replaceCall(call, index, data));

        } else if (call.isExecutable() && looksLikeEmpty(data)) {
            return Optional.of(ReplaceWithEmptyCommand.replaceCall(call, index, data));

        } else if (call.isLocalSetting() && looksLikeCall(data)) {
            return Optional.of(ReplaceWithCallCommand.replaceSetting(call, index, data));

        } else if (call.isLocalSetting() && looksLikeEmpty(data)) {
            return Optional.of(ReplaceWithEmptyCommand.replaceSetting(call, index, data));

        } else if (call.isEmptyLine() && looksLikeCall(data)) {
            return Optional.of(ReplaceWithCallCommand.replaceEmpty(call, index, data));

        } else if (call.isEmptyLine() && looksLikeSetting(data)) {
            return Optional.of(ReplaceWithSettingCommand.replaceEmpty(call, index, data));

        } else {
            return Optional.of(new InsertNewCellCommand(call, column));
        }
    }

    public Optional<? extends EditorCommand> collectForRemoval(final RobotKeywordCall call,
            final List<Integer> columns) {

        final List<String> data = ExecutablesRowView.rowData(call);
        final int[] filteredColumns = columns.stream()
                .mapToInt(Integer::valueOf)
                .filter(i -> i >= 0 && i < data.size())
                .sorted()
                .toArray();
        if (filteredColumns.length == 0) {
            return Optional.empty();
        }

        for (int i = filteredColumns.length - 1; i >= 0; i--) {
            data.remove(filteredColumns[i]);
        }

        final int index = call.getIndex();
        if (call.isExecutable() && looksLikeSetting(data)) {
            return Optional.of(ReplaceWithSettingCommand.replaceCall(call, index, data));

        } else if (call.isExecutable() && looksLikeEmpty(data)) {
            return Optional.of(ReplaceWithEmptyCommand.replaceCall(call, index, data));

        } else if (call.isExecutable()) {
            return Optional.of(new RemoveStepCellCommand(call, filteredColumns));

        } else if (call.isLocalSetting() && looksLikeCall(data)) {
            return Optional.of(ReplaceWithCallCommand.replaceSetting(call, index, data));

        } else if (call.isLocalSetting() && looksLikeEmpty(data)) {
            return Optional.of(ReplaceWithEmptyCommand.replaceSetting(call, index, data));

        } else if (call.isLocalSetting()) {
            return Optional.of(new RemoveStepCellCommand(call, filteredColumns));

        } else if (call.isEmptyLine() && looksLikeCall(data)) {
            return Optional.of(ReplaceWithCallCommand.replaceEmpty(call, index, data));

        } else if (call.isEmptyLine() && looksLikeSetting(data)) {
            return Optional.of(ReplaceWithSettingCommand.replaceEmpty(call, index, data));

        } else if (call.isEmptyLine()) {
            return Optional.of(new RemoveStepCellCommand(call, filteredColumns));
        }
        return Optional.empty();
    }

    private boolean looksLikeSetting(final List<String> data) {
        return !data.isEmpty() && data.get(0).startsWith("[") && data.get(0).endsWith("]");
    }

    private boolean looksLikeEmpty(final List<String> data) {
        if (data.isEmpty()) {
            return true;
        }
        if (data.get(0).trim().startsWith("#")) {
            return true;

        } else if (data.get(0).equals("\\") || data.get(0).isEmpty()) {
            return data.size() == 1 || data.get(1).trim().startsWith("#");
        }
        return false;
    }

    private boolean looksLikeCall(final List<String> data) {
        return !looksLikeSetting(data) && !looksLikeEmpty(data);
    }
}
