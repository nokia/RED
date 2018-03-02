/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotEmptyLine;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordCallNameCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    private RobotKeywordCall actualCall;

    private final String newName;

    private final String oldName;

    private final List<EditorCommand> executedCommands = new ArrayList<>();

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

        final boolean shouldShiftNameFromArgs = newName == null;
        String nameToSet = shouldShiftNameFromArgs ? extractNameFromArguments(keywordCall.getArguments()) : newName;
        nameToSet = nameToSet.isEmpty() && !keywordCall.getArguments().isEmpty() ? "\\" : nameToSet;

        actualCall = getConvertedCall(nameToSet);

        if (shouldShiftNameFromArgs) {
            removeFirstArgument(actualCall);
        }
    }

    private RobotKeywordCall getConvertedCall(final String nameToSet) {
        final int index = keywordCall.getIndex();
        final IRobotCodeHoldingElement parent = keywordCall.getParent();
        final EditorCommand command = createChangeCommand(nameToSet);
        command.execute();
        executedCommands.add(command);
        return parent.getChildren().get(index);
    }

    private EditorCommand createChangeCommand(final String nameToSet) {
        if (looksLikeSetting(nameToSet)) {
            return createChangeToSettingCommand(nameToSet);
        } else if (looksLikeEmpty(nameToSet) && keywordCall.getArguments().isEmpty()
                && keywordCall.getComment().isEmpty()) {
            return createChangeToEmptyCommand(nameToSet);
        } else {
            return createChangeToCallCommand(nameToSet);
        }
    }

    private EditorCommand createChangeToCallCommand(final String nameToSet) {
        if (keywordCall.isExecutable()) {
            return new SetSimpleKeywordCallName(eventBroker, keywordCall, nameToSet);
        } else if (keywordCall instanceof RobotDefinitionSetting) {
            return new ConvertSettingToCall(eventBroker, (RobotDefinitionSetting) keywordCall, nameToSet);
        } else {
            return new ConvertEmptyToCall(eventBroker, (RobotEmptyLine) keywordCall, nameToSet);
        }
    }

    private EditorCommand createChangeToSettingCommand(final String nameToSet) {
        if (keywordCall.isExecutable()) {
            return new ConvertCallToSetting(eventBroker, keywordCall, nameToSet);
        } else if (keywordCall instanceof RobotDefinitionSetting) {
            if (isDifferentSetting(nameToSet)) {
                return new ConvertSettingToSetting(eventBroker, (RobotDefinitionSetting) keywordCall, nameToSet);
            } else {
                return new SetSimpleKeywordCallName(eventBroker, keywordCall, nameToSet);
            }
        } else {
            return new ConvertEmptyToSetting(eventBroker, (RobotEmptyLine) keywordCall, nameToSet);
        }
    }

    private EditorCommand createChangeToEmptyCommand(final String nameToSet) {
        if (keywordCall.isExecutable()) {
            return new ConvertCallToEmpty(eventBroker, keywordCall, nameToSet);
        } else if (keywordCall instanceof RobotDefinitionSetting) {
            return new ConvertSettingToEmpty(eventBroker, (RobotDefinitionSetting) keywordCall, nameToSet);
        } else {
            return new SetSimpleKeywordCallName(eventBroker, keywordCall, nameToSet);
        }
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

    private boolean looksLikeEmpty(final String name) {
        return Pattern.matches("^[\\s]*$", name);
    }

    private void removeFirstArgument(final RobotKeywordCall actualCall) {
        final List<String> newArguments = new ArrayList<>(actualCall.getArguments());
        if (!newArguments.isEmpty()) {
            newArguments.remove(0);
        }
        final SetSimpleKeywordCallArguments command = new SetSimpleKeywordCallArguments(eventBroker, actualCall,
                newArguments);
        executedCommands.add(command);
        command.execute();
    }

    public RobotKeywordCall getActualCall() {
        return actualCall;
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        final List<EditorCommand> undoCommands = new ArrayList<>();
        for (final EditorCommand executedCommand : executedCommands) {
            undoCommands.addAll(0, executedCommand.getUndoCommands());
        }
        return newUndoCommands(undoCommands);
    }
}
