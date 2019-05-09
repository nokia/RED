/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateArgumentSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetCodeHolderNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CodeTableValuesChangingCommandsCollector {

    public List<? extends EditorCommand> collectForRemoval(final RobotElement element, final List<Integer> columns) {
        final List<EditorCommand> commands = new ArrayList<>();

        if (element instanceof RobotCodeHoldingElement<?>) {
            final RobotCodeHoldingElement<?> codeHolder = (RobotCodeHoldingElement<?>) element;

            if (columns.contains(Integer.valueOf(0))) {
                commands.add(new SetCodeHolderNameCommand(codeHolder, null));
            }

            if (element instanceof RobotKeywordDefinition) {
                final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) element;

                final RobotKeywordCall argumentsSetting = keywordDef.getArgumentsSetting();
                if (argumentsSetting != null) {
                    final List<String> data = ExecutablesRowView.rowData(argumentsSetting);
                    final List<Integer> filteredColumns = columns.stream()
                            .mapToInt(Integer::valueOf)
                            .filter(i -> i > 0 && i < data.size())
                            .sorted()
                            .mapToObj(Integer::valueOf)
                            .collect(toList());
                    for (int i = filteredColumns.size() - 1; i >= 0; i--) {
                        data.remove(filteredColumns.get(i).intValue());
                    }

                    if (data.size() == 1) {
                        // there is only [Argument] cell left
                        commands.add(new DeleteKeywordCallCommand(newArrayList(argumentsSetting)));

                    } else {
                        new KeywordCallsTableValuesChangingCommandsCollector()
                                .collectForRemoval(argumentsSetting, filteredColumns)
                                .ifPresent(commands::add);
                    }
                }
            }
        } else {
            new KeywordCallsTableValuesChangingCommandsCollector()
                    .collectForRemoval((RobotKeywordCall) element, columns)
                    .ifPresent(commands::add);
        }
        return commands;
    }

    public Optional<? extends EditorCommand> collectForChange(final RobotElement element, final String value,
            final int column) {

        if (element instanceof RobotCodeHoldingElement<?>) {
            final RobotCodeHoldingElement<?> codeHolder = (RobotCodeHoldingElement<?>) element;

            if (column == 0) {
                return Optional.of(new SetCodeHolderNameCommand(codeHolder, value));

            } else if (column > 0 && element instanceof RobotKeywordDefinition) {
                final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) element;

                final RobotKeywordCall argumentsSetting = keywordDef.getArgumentsSetting();
                if (argumentsSetting == null) {
                    return Optional.of(new CreateArgumentSettingCommand(keywordDef, column, value));
                } else {
                    return new KeywordCallsTableValuesChangingCommandsCollector().collectForUpdate(argumentsSetting,
                            value, column);
                }
            }
            return Optional.empty();

        } else if (element instanceof RobotKeywordCall && value != null) {
            return new KeywordCallsTableValuesChangingCommandsCollector().collectForUpdate((RobotKeywordCall) element,
                    value, column);

        } else {
            return Optional.empty();
        }
    }
}
