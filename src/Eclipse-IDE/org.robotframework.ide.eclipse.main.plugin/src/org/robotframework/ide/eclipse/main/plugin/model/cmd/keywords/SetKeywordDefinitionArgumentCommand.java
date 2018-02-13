/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand2;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class SetKeywordDefinitionArgumentCommand extends EditorCommand {

    private final RobotKeywordDefinition definition;

    private final int index;

    private final String value;

    private final List<EditorCommand> undoCommands = new ArrayList<>();

    public SetKeywordDefinitionArgumentCommand(final RobotKeywordDefinition definition, final int index,
            final String value) {
        this.definition = definition;
        this.index = index;
        this.value = value;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotDefinitionSetting setting = definition.getArgumentsSetting();

        final List<String> currentArguments = setting == null ? new ArrayList<>() : setting.getArguments();
        final List<String> newArguments = SetKeywordCallArgumentCommand2.prepareArgumentsList(currentArguments, index,
                value);
        final boolean areAllEmpty = areAllEmpty(newArguments);

        if (setting == null && areAllEmpty) {
            // there is no setting and we have no arguments to set
            return;

        } else if (setting != null && areAllEmpty) {
            // there is a setting, but we don't have arguments
            final DeleteArgumentSettingCommand command = new DeleteArgumentSettingCommand(setting);
            command.setEventBroker(eventBroker);
            command.execute();

            undoCommands.addAll(command.getUndoCommands());

        } else if (setting == null) {
            // there is no setting, but we have arguments to set
            final CreateArgumentSettingCommand command = new CreateArgumentSettingCommand(definition, newArguments);
            command.setEventBroker(eventBroker);
            command.execute();

            undoCommands.addAll(command.getUndoCommands());

        } else if (!newArguments.equals(setting.getArguments())) {
            // there is a setting and we have arguments which are different than current
            final SetKeywordCallArgumentCommand2 command = new SetKeywordCallArgumentCommand2(setting, index, value);
            command.setEventBroker(eventBroker);
            command.execute();

            undoCommands.addAll(command.getUndoCommands());
        }
    }

    private boolean areAllEmpty(final List<String> arguments) {
        for (final String argument : arguments) {
            if (argument != null && !argument.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return undoCommands;
    }

    private static class CreateArgumentSettingCommand extends EditorCommand {

        private final RobotKeywordDefinition definition;

        private final List<String> arguments;

        private RobotDefinitionSetting setting;

        public CreateArgumentSettingCommand(final RobotKeywordDefinition definition, final List<String> arguments) {
            this.definition = definition;
            this.arguments = arguments;
        }

        @Override
        public void execute() throws CommandExecutionException {
            setting = definition.createSetting(0, RobotTokenType.KEYWORD_SETTING_ARGUMENTS.getRepresentation().get(0),
                    arguments, "");

            RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(setting)
                .send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, definition);
        }

        @Override
        public List<EditorCommand> getUndoCommands() {
            return newUndoCommands(new DeleteArgumentSettingCommand(setting));
        }
    }

    private static class DeleteArgumentSettingCommand extends EditorCommand {

        private RobotKeywordDefinition keyword;
        private final RobotDefinitionSetting argSetting;

        private List<String> oldArguments;

        public DeleteArgumentSettingCommand(final RobotDefinitionSetting argSetting) {
            this.argSetting = argSetting;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute() throws CommandExecutionException {
            keyword = (RobotKeywordDefinition) argSetting.getParent();

            oldArguments = argSetting.getArguments();

            keyword.getChildren().remove(argSetting);
            keyword.getLinkedElement().removeElement((AModelElement<UserKeyword>) argSetting.getLinkedElement());

            RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(argSetting)
                .send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, keyword);
        }

        @Override
        public List<EditorCommand> getUndoCommands() {
            return newUndoCommands(new CreateArgumentSettingCommand(keyword, oldArguments));
        }
    }
}
