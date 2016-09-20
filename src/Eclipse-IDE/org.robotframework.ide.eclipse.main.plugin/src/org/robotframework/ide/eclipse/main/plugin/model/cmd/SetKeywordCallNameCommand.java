/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class SetKeywordCallNameCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    private final String newName;

    private final String oldName;

    private List<EditorCommand> executedCommands;

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

        final RobotKeywordCall actualCall;
        if (keywordCall.isExecutable()) {
            if (looksLikeSetting(nameToSet)) {
                actualCall = changeToSetting(nameToSet);
            } else {
                actualCall = changeName(nameToSet);
            }
        } else {
            if (!looksLikeSetting(nameToSet)) {
                actualCall = changeToCall(nameToSet);
            } else if (!isDifferentSetting(nameToSet)) {
                actualCall = changeName(nameToSet);
            } else {
                actualCall = changeBetweenSettings(nameToSet);
            }
        }

        if (shouldShiftNameFromArgs) {
            removeFirstArgument(actualCall);
        }
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

    private RobotKeywordCall changeToSetting(final String settingName) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();

        final int index = keywordCall.getIndex();
        final int lastSettingIndex = parent.indexOfLastSetting();

        final MoveKeywordCall moveCommand = new MoveKeywordCall(parent, index, lastSettingIndex + 1);
        moveCommand.execute();
        
        final ConvertCallToSetting convertCommand = new ConvertCallToSetting(eventBroker, keywordCall, settingName);
        convertCommand.execute();

        // when doing undo we also want to firstly move and then convert
        executedCommands.add(convertCommand);
        executedCommands.add(moveCommand);

        return parent.getChildren().get(lastSettingIndex + 1);
    }

    private RobotKeywordCall changeToCall(final String name) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();
        final RobotDefinitionSetting setting = (RobotDefinitionSetting) keywordCall;

        final int index = setting.getIndex();
        final int lastSettingIndex = parent.indexOfLastSetting();
        
        final MoveKeywordCall moveCommand = new MoveKeywordCall(parent, index, lastSettingIndex);
        moveCommand.execute();
        
        final ConvertSettingToCall convertCommand = new ConvertSettingToCall(eventBroker, setting, name);
        convertCommand.execute();
        
        // when doing undo we also want to firstly move and then convert
        executedCommands.add(convertCommand);
        executedCommands.add(moveCommand);

        return parent.getChildren().get(lastSettingIndex);
    }

    private void removeFirstArgument(final RobotKeywordCall actualCall) {
        final List<String> newArguments = newArrayList(actualCall.getArguments());
        if (!newArguments.isEmpty()) {
            newArguments.remove(0);
        }
        final SetSimpleKeywordCallArguments command = new SetSimpleKeywordCallArguments(eventBroker, actualCall, newArguments);
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

    private static class ConvertCallToSetting extends EditorCommand {

        private final IEventBroker eventBroker;

        private final RobotKeywordCall call;

        private final String settingName;

        private RobotDefinitionSetting newSetting;

        public ConvertCallToSetting(final IEventBroker eventBroker, final RobotKeywordCall call,
                final String settingName) {
            this.eventBroker = eventBroker;
            this.call = call;
            this.settingName = settingName;
        }

        @Override
        public void execute() throws CommandExecutionException {
            final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) call.getParent();

            final int index = call.getIndex();
            parent.removeChild(call);
            newSetting = parent.createSetting(index, settingName, call.getArguments(), call.getComment());

            RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(newSetting)
                .send(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
        }

        @Override
        public List<EditorCommand> getUndoCommands() {
            return newUndoCommands(new ConvertSettingToCall(eventBroker, newSetting, call.getName()));
        }
    }

    private static class ConvertSettingToSetting extends EditorCommand {

        private final IEventBroker eventBroker;

        private final RobotKeywordCall call;

        private final String settingName;

        private RobotDefinitionSetting newSetting;

        public ConvertSettingToSetting(final IEventBroker eventBroker, final RobotKeywordCall call,
                final String settingName) {
            this.eventBroker = eventBroker;
            this.call = call;
            this.settingName = settingName;
        }

        @Override
        public void execute() throws CommandExecutionException {
            final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) call.getParent();

            final int index = call.getIndex();
            parent.removeChild(call);
            newSetting = parent.createSetting(index, settingName, call.getArguments(), call.getComment());

            RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(newSetting)
                .send(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
        }

        @Override
        public List<EditorCommand> getUndoCommands() {
            return newUndoCommands(new ConvertSettingToSetting(eventBroker, newSetting, "[" + call.getName() + "]"));
        }
    }
    
    private static class ConvertSettingToCall extends EditorCommand {

        private final IEventBroker eventBroker;

        private final RobotDefinitionSetting setting;

        private final String name;

        private RobotKeywordCall newCall;

        public ConvertSettingToCall(final IEventBroker eventBroker, final RobotDefinitionSetting setting,
                final String name) {
            this.eventBroker = eventBroker;
            this.setting = setting;
            this.name = name;
        }

        @Override
        public void execute() throws CommandExecutionException {
            final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) setting.getParent();

            final int index = setting.getIndex();
            parent.removeChild(setting);
            newCall = parent.createKeywordCall(index, name, setting.getArguments(), setting.getComment());

            RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(newCall)
                .send(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
        }

        @Override
        public List<EditorCommand> getUndoCommands() {
            return newUndoCommands(new ConvertCallToSetting(eventBroker, newCall, "[" + setting.getName() + "]"));
        }
    }

    private static class MoveKeywordCall extends EditorCommand {

        private final RobotCodeHoldingElement<?> parent;

        private final int index;

        private final int targetIndex;

        public MoveKeywordCall(final RobotCodeHoldingElement<?> parent, final int index, final int targetIndex) {
            this.parent = parent;
            this.index = index;
            this.targetIndex = targetIndex;
        }

        @Override
        public void execute() throws CommandExecutionException {
            if (index == targetIndex) {
                return;
            }
            final RobotKeywordCall removed = parent.getChildren().remove(index);
            parent.getChildren().add(targetIndex, removed);
        }

        @Override
        public List<EditorCommand> getUndoCommands() {
            return newUndoCommands(new MoveKeywordCall(parent, targetIndex, index));
        }
    }
}
