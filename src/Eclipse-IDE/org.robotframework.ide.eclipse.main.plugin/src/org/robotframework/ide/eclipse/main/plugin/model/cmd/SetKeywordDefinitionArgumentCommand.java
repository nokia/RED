/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.presenter.update.KeywordTableModelUpdater;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordDefinitionArgumentCommand extends EditorCommand {

    private final RobotKeywordDefinition definition;

    private final int index;

    private final String value;

    public SetKeywordDefinitionArgumentCommand(final RobotKeywordDefinition definition, final int index,
            final String value) {
        this.definition = definition;
        this.index = index;
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws CommandExecutionException {
        RobotDefinitionSetting argumentsSetting = definition.getArgumentsSetting();
        if (argumentsSetting == null && value.isEmpty()) {
            return;
        }
        if (argumentsSetting == null) {
            argumentsSetting = definition.createSetting(0, "[" + RobotKeywordDefinition.ARGUMENTS + "]",
                    new ArrayList<String>(), "");

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, definition);
        }

        final List<String> arguments = argumentsSetting.getArguments();
        boolean changed = false;

        for (int i = arguments.size(); i <= index; i++) {
            arguments.add("\\");
            changed = true;
        }
        changed |= index < arguments.size() && !arguments.get(index).equals(value);
        arguments.set(index, value == null || value.isEmpty() ? "\\" : value);
        for (int i = arguments.size() - 1; i >= 0; i--) {
            if (!arguments.get(i).equals("\\")) {
                break;
            }
            arguments.set(i, null);
        }
        if (changed) {
            argumentsSetting.resetStored();
            final KeywordTableModelUpdater updater = new KeywordTableModelUpdater();
            if (value != null) {
                for (int i = arguments.size() - 1; i >= 0; i--) {
                    updater.update(argumentsSetting.getLinkedElement(), i, arguments.get(i));
                }
            } else {
                updater.update(argumentsSetting.getLinkedElement(), index, value);
            }

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ARGUMENT_CHANGE, definition);
        }

        boolean allAreEmpty = true;
        for (final String argument : arguments) {
            if (argument != null) {
                allAreEmpty &= argument.trim().isEmpty();
            }
        }
        if (allAreEmpty) {
            definition.getChildren().remove(argumentsSetting);
            definition.getLinkedElement()
                    .removeUnitSettings((AModelElement<UserKeyword>) argumentsSetting.getLinkedElement());

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, definition);
        }
    }
}
