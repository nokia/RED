/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesTableModelUpdater;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotEmptyLine;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordCallArgumentCommand2 extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    private final int index;

    private final String value;

    private final List<EditorCommand> undoOperations = new ArrayList<>();

    public SetKeywordCallArgumentCommand2(final IEventBroker eventBroker, final RobotKeywordCall keywordCall,
            final int index, final String value) {
        this.eventBroker = eventBroker;
        this.keywordCall = keywordCall;
        this.index = index;
        this.value = value;
    }

    public SetKeywordCallArgumentCommand2(final RobotKeywordCall keywordCall, final int index, final String value) {
        this.keywordCall = keywordCall;
        this.index = index;
        this.value = value;
    }

    @Override
    public void execute() throws CommandExecutionException {
        // TODO : replace SetKeywordCallArgument with this implementation since this
        // one seems simpler

        // return if there is nothing to change and DO NOT convert RobotEmptyLine
        if (keywordCall instanceof RobotEmptyLine && value == null) {
            return;
        }

        // convert keywordCall from RobotEmptyLine to simple RobotKeywordCall if needed
        final RobotKeywordCall keywordCallToUpdate = keywordCall instanceof RobotEmptyLine
                ? changeEmptyToExecutable(keywordCall)
                : keywordCall;

        final List<String> oldArguments = keywordCallToUpdate.getArguments();

        final Optional<String> newName = prepareNewName(keywordCallToUpdate);
        final List<String> arguments = prepareArgumentsList(keywordCallToUpdate.getArguments(), index, value);

        if (newName.isPresent()) {
            final SetSimpleKeywordCallName changeNameCommand = new SetSimpleKeywordCallName(eventBroker,
                    keywordCallToUpdate, newName.get());
            changeNameCommand.execute();

            undoOperations.addAll(changeNameCommand.getUndoCommands());
        }
        if (!arguments.equals(oldArguments)) {
            undoOperations.add(new SetSimpleKeywordCallArguments(eventBroker, keywordCallToUpdate, oldArguments));

            updateModelElement(keywordCallToUpdate, arguments);
            keywordCallToUpdate.resetStored();

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, keywordCallToUpdate);
        }
    }

    private Optional<String> prepareNewName(final RobotKeywordCall keywordCall) {
        if (index >= keywordCall.getArguments().size() && !(value == null || value.isEmpty())
                && keywordCall.getName().isEmpty()) {
            return Optional.of("\\");
        }
        return Optional.empty();
    }

    public static List<String> prepareArgumentsList(final List<String> originalList, final int index,
            final String value) {
        final List<String> arguments = new ArrayList<>(originalList);
        if (index >= arguments.size() && (value == null || value.isEmpty())) {
            return arguments;
        }
        if (index >= arguments.size()) {
            for (int i = arguments.size(); i < index; i++) {
                arguments.add("\\");
            }
            arguments.add(value);
        } else if ((value == null || value.isEmpty()) && index == arguments.size() - 1) {
            arguments.remove(index);
            int i = arguments.size() - 1;
            while (i >= 0 && arguments.get(i).equals("\\")) {
                arguments.remove(i);
                i--;
            }
        } else if (value == null) {
            arguments.remove(index);
        } else if (value.isEmpty()) {
            arguments.set(index, "\\");
        } else {
            arguments.set(index, value);
        }
        return arguments;
    }

    protected static void updateModelElement(final RobotKeywordCall keywordCall, final List<String> arguments) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();
        final IExecutablesTableModelUpdater<?> updater = parent.getModelUpdater();
        updater.setArguments(keywordCall.getLinkedElement(), arguments);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(undoOperations);
    }

    private RobotKeywordCall changeEmptyToExecutable(final RobotKeywordCall keywordCall) {
        final ConvertEmptyToCall command = new ConvertEmptyToCall(eventBroker, (RobotEmptyLine) keywordCall, "");
        command.execute();
        undoOperations.addAll(command.getUndoCommands());
        return command.getNewCall();
    }
}
