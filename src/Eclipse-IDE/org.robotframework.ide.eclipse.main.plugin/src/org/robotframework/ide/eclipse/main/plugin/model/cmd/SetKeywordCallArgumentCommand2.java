/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.presenter.update.IExecutablesTableModelUpdater;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CompoundEditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.base.Optional;

public class SetKeywordCallArgumentCommand2 extends EditorCommand {

    private final RobotKeywordCall keywordCall;
    private final int index;
    private final String value;
    
    private final List<EditorCommand> undoOperations = new ArrayList<>();

    public SetKeywordCallArgumentCommand2(final RobotKeywordCall keywordCall, final int index, final String value) {
        this.keywordCall = keywordCall;
        this.index = index;
        this.value = value;
    }

    @Override
    public void execute() throws CommandExecutionException {
        // TODO : replace SetKeywordCallArgument with this implementation since this
        // one seems simpler

        final List<String> oldArguments = keywordCall.getArguments();

        final Optional<String> newName = prepareNewName();
        final List<String> arguments = prepareArgumentsList(keywordCall.getArguments(), index, value);

        if (newName.isPresent()) {
            final SetSimpleKeywordCallName changeNameCommand = new SetSimpleKeywordCallName(eventBroker, keywordCall, newName.get());
            changeNameCommand.execute();

            undoOperations.add(changeNameCommand.getUndoCommand());
        }
        if (!arguments.equals(oldArguments)) {
            undoOperations.add(new SetSimpleKeywordCallArguments(eventBroker, keywordCall, oldArguments));

            updateModelElement(arguments);
            keywordCall.resetStored();

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, keywordCall);
        }
    }

    private Optional<String> prepareNewName() {
        if (index >= keywordCall.getArguments().size() && !(value == null || value.isEmpty())
                && keywordCall.getName().isEmpty()) {
            return Optional.of("\\");
        }
        return Optional.absent();
    }

    public static List<String> prepareArgumentsList(final List<String> originalList, final int index,
            final String value) {
        final List<String> arguments = newArrayList(originalList);
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

    protected void updateModelElement(final List<String> arguments) {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();
        final IExecutablesTableModelUpdater<?> updater = parent.getModelUpdater();
        updater.setArguments(keywordCall.getLinkedElement(), arguments);
    }

    @Override
    public EditorCommand getUndoCommand() {
        return newUndoCompoundCommand(new CompoundEditorCommand(this, undoOperations));
    }
}
