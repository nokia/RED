/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.presenter.update.KeywordTableModelUpdater;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

import com.google.common.base.Optional;

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

        final Optional<List<String>> arguments = prepareArgumentsList(argumentsSetting);

        if (argumentsSetting == null && !arguments.isPresent()) {
            // there is no setting and we have no arguments to set
            return;

        } else if (argumentsSetting != null && !arguments.isPresent()) {
            // there is a setting, but we don't have arguments
            definition.getChildren().remove(argumentsSetting);
            definition.getLinkedElement()
                    .removeUnitSettings((AModelElement<UserKeyword>) argumentsSetting.getLinkedElement());

            RedEventBroker.using(eventBroker)
                    .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(argumentsSetting)
                    .send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, definition);

        } else if (argumentsSetting == null) {
            // there is no setting, but we have arguments to set
            argumentsSetting = definition.createSetting(0, "[" + RobotKeywordDefinition.ARGUMENTS + "]",
                    new ArrayList<String>(), "");
            updateModel(argumentsSetting, arguments.get());

            RedEventBroker.using(eventBroker)
                    .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(argumentsSetting)
                    .send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, definition);

        } else if (!arguments.get().equals(argumentsSetting.getArguments())) {
            // there is a setting and we have arguments which are different than current
            updateModel(argumentsSetting, arguments.get());
            argumentsSetting.resetStored();

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ARGUMENT_CHANGE, definition);
        }
    }

    private Optional<List<String>> prepareArgumentsList(final RobotDefinitionSetting setting) {
        final List<String> arguments = setting == null ? new ArrayList<String>() : newArrayList(setting.getArguments());

        for (int i = arguments.size(); i <= index; i++) {
            arguments.add("\\");
        }
        arguments.set(index, value == null || value.trim().isEmpty() ? "\\" : value);
        for (int i = arguments.size() - 1; i >= 0; i--) {
            if (!arguments.get(i).equals("\\")) {
                break;
            }
            arguments.set(i, null);
        }
        return areAllEmpty(arguments) ? Optional.<List<String>> absent() : Optional.of(arguments);
    }

    private boolean areAllEmpty(final List<String> arguments) {
        for (final String argument : arguments) {
            if (argument != null && !argument.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void updateModel(final RobotDefinitionSetting argumentsSetting, final List<String> arguments) {
        final KeywordTableModelUpdater updater = new KeywordTableModelUpdater();
        if (value != null) {
            for (int i = arguments.size() - 1; i >= 0; i--) {
                updater.updateArgument(argumentsSetting.getLinkedElement(), i, arguments.get(i));
            }
        } else {
            updater.updateArgument(argumentsSetting.getLinkedElement(), index, value);
        }
    }
}
