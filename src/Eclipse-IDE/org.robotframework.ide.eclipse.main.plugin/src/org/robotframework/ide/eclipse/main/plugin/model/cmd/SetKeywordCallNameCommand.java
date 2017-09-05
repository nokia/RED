/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotEmptyLine;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordCallNameCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    private final String newName;

    private final String oldName;

    private List<EditorCommand> executedCommands;

    public SetKeywordCallNameCommand(final IEventBroker eventBroker, final RobotKeywordCall keywordCall,
            final String name) {
        this.eventBroker = eventBroker;
        this.keywordCall = keywordCall;
        this.newName = name;
        this.oldName = keywordCall.getName();
    }

    public SetKeywordCallNameCommand(final RobotKeywordCall keywordCall, final String name) {
        this.keywordCall = keywordCall;
        this.newName = name;
        this.oldName = keywordCall.getName();
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (oldName.equals(newName)) {
            return;
        }
        executedCommands = new ArrayList<>();

        final boolean shouldShiftNameFromArgs = newName == null;
        String nameToSet = shouldShiftNameFromArgs ? extractNameFromArguments(keywordCall.getArguments()) : newName;
        nameToSet = nameToSet.isEmpty() && !keywordCall.getArguments().isEmpty() ? "\\" : nameToSet;

        final RobotKeywordCall actualCall = getConvertedCall(nameToSet);

        if (shouldShiftNameFromArgs) {
            removeFirstArgument(actualCall);
        }
    }

    private RobotKeywordCall getConvertedCall(final String nameToSet) {
        final RobotKeywordCall actualCall;
        if (looksLikeSetting(nameToSet)) {
            actualCall = changeToSetting(nameToSet);
        } else if (looksLikeEmpty(nameToSet) && keywordCall.getArguments().isEmpty()
                && keywordCall.getComment().isEmpty()) {
            actualCall = changeToEmpty(nameToSet);
        } else {
            actualCall = changeToCall(nameToSet);
        }
        return actualCall;
    }

    private RobotKeywordCall changeToCall(final String nameToSet) {
        final RobotKeywordCall actualCall;
        if (keywordCall.isExecutable()) {
            actualCall = changeName(nameToSet);
        } else if (keywordCall instanceof RobotDefinitionSetting) {
            actualCall = changeSettingToCall(nameToSet);
        } else {
            actualCall = changeEmptyToCall(nameToSet);
        }
        return actualCall;
    }

    private RobotKeywordCall changeToSetting(final String nameToSet) {
        final RobotKeywordCall actualCall;
        if (keywordCall.isExecutable()) {
            actualCall = changeCallToSetting(nameToSet);
        } else if (keywordCall instanceof RobotDefinitionSetting) {
            if (isDifferentSetting(nameToSet)) {
                actualCall = changeBetweenSettings(nameToSet);
            } else {
                actualCall = changeName(nameToSet);
            }
        } else {
            actualCall = changeEmptyToSetting(nameToSet);
        }
        return actualCall;
    }

    private RobotKeywordCall changeToEmpty(final String nameToSet) {
        final RobotKeywordCall actualCall;
        if (keywordCall.isExecutable()) {
            actualCall = changeCallToEmpty(nameToSet);
        } else if (keywordCall instanceof RobotDefinitionSetting) {
            actualCall = changeSettingToEmpty(nameToSet);
        } else {
            actualCall = changeName(nameToSet);
        }
        return actualCall;
    }

    private RobotKeywordCall changeName(final String nameToSet) {
        final EditorCommand command = new SetSimpleKeywordCallName(eventBroker, keywordCall, nameToSet);
        executedCommands.add(command);
        command.execute();
        return keywordCall;
    }

    private String extractNameFromArguments(final List<String> arguments) {
        return arguments.isEmpty() ? "" : arguments.get(0);
    }

    private boolean isDifferentSetting(final String name) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();
        final RobotTokenType tokenType = parent.getSettingDeclarationTokenTypeFor(name);
        return !keywordCall.getLinkedElement().getDeclaration().getTypes().contains(tokenType);
    }

    private boolean looksLikeSetting(final String name) {
        return name.startsWith("[") && name.endsWith("]");
    }

    private RobotKeywordCall changeBetweenSettings(final String name) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();

        final int index = keywordCall.getIndex();

        final ConvertSettingToSetting convertCommand = new ConvertSettingToSetting(eventBroker, keywordCall, name);
        convertCommand.execute();
        executedCommands.add(convertCommand);

        return parent.getChildren().get(index);
    }

    private RobotKeywordCall changeCallToSetting(final String settingName) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();

        final int index = keywordCall.getIndex();

        final ConvertCallToSetting convertCommand = new ConvertCallToSetting(eventBroker, keywordCall, settingName);
        convertCommand.execute();

        executedCommands.add(convertCommand);

        return parent.getChildren().get(index);
    }

    private RobotKeywordCall changeCallToEmpty(final String emptyName) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();

        final int index = keywordCall.getIndex();

        final ConvertCallToEmpty convertCommand = new ConvertCallToEmpty(eventBroker, keywordCall, emptyName);
        convertCommand.execute();

        executedCommands.add(convertCommand);

        return parent.getChildren().get(index);
    }

    private RobotKeywordCall changeSettingToEmpty(final String emptyName) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();

        final int index = keywordCall.getIndex();

        final ConvertSettingToEmpty convertCommand = new ConvertSettingToEmpty(eventBroker, keywordCall, emptyName);
        convertCommand.execute();

        executedCommands.add(convertCommand);

        return parent.getChildren().get(index);
    }

    private RobotKeywordCall changeEmptyToSetting(final String settingName) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();

        final int index = keywordCall.getIndex();

        final ConvertEmptyToSetting convertCommand = new ConvertEmptyToSetting(eventBroker,
                (RobotEmptyLine) keywordCall, settingName);
        convertCommand.execute();

        executedCommands.add(convertCommand);

        return parent.getChildren().get(index);
    }

    private RobotKeywordCall changeSettingToCall(final String name) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();
        final RobotDefinitionSetting setting = (RobotDefinitionSetting) keywordCall;

        final int index = setting.getIndex();

        final ConvertSettingToCall convertCommand = new ConvertSettingToCall(eventBroker, setting, name);
        convertCommand.execute();

        executedCommands.add(convertCommand);

        return parent.getChildren().get(index);
    }

    private boolean looksLikeEmpty(final String name) {
        return Pattern.matches("^[\\s]*$", name);
    }

    private RobotKeywordCall changeEmptyToCall(final String name) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();
        final RobotEmptyLine empty = (RobotEmptyLine) keywordCall;

        final int index = empty.getIndex();

        final ConvertEmptyToCall convertCommand = new ConvertEmptyToCall(eventBroker, empty, name);
        convertCommand.execute();

        executedCommands.add(convertCommand);

        return parent.getChildren().get(index);
    }

    private void removeFirstArgument(final RobotKeywordCall actualCall) {
        final List<String> newArguments = newArrayList(actualCall.getArguments());
        if (!newArguments.isEmpty()) {
            newArguments.remove(0);
        }
        final SetSimpleKeywordCallArguments command = new SetSimpleKeywordCallArguments(eventBroker, actualCall,
                newArguments);
        executedCommands.add(command);
        command.execute();
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        final List<EditorCommand> undoCommands = newArrayList();
        for (final EditorCommand executedCommand : executedCommands) {
            undoCommands.addAll(0, executedCommand.getUndoCommands());
        }
        return newUndoCommands(undoCommands);
    }
}
