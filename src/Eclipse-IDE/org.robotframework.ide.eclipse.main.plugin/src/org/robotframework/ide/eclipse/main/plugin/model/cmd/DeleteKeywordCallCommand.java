/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteKeywordCallCommand extends EditorCommand {

    protected final List<? extends RobotKeywordCall> callsToDelete;

    private final String eventTopic;

    protected List<Integer> deletedCallsIndexes = newArrayList();

    public DeleteKeywordCallCommand(final List<? extends RobotKeywordCall> callsToDelete) {
        this(callsToDelete, RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED);
    }

    protected DeleteKeywordCallCommand(final List<? extends RobotKeywordCall> callsToDelete, final String topic) {
        this.callsToDelete = callsToDelete;
        this.eventTopic = topic;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (callsToDelete.isEmpty()) {
            return;
        }
        for (final RobotKeywordCall call : callsToDelete) {
            deletedCallsIndexes.add(call.getIndex());
        }
        
        final Set<IRobotCodeHoldingElement> parentsWhereRemovalWasPerformed = new HashSet<>();

        for (final RobotKeywordCall call : callsToDelete) {
            final IRobotCodeHoldingElement parent = call.getParent();
            parent.removeChild(call);

            parentsWhereRemovalWasPerformed.add(parent);
        }
        for (final IRobotCodeHoldingElement parent : parentsWhereRemovalWasPerformed) {
            eventBroker.send(eventTopic, parent);
        }
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(setupUndoCommandsForDeletedCalls());
    }

    private List<EditorCommand> setupUndoCommandsForDeletedCalls() {
        final List<EditorCommand> commands = newArrayList();
        if (callsToDelete.size() == deletedCallsIndexes.size()) {
            for (int i = 0; i < callsToDelete.size(); i++) {
                final RobotKeywordCall call = callsToDelete.get(i);
                commands.add(new InsertKeywordCallsCommand(call.getParent(),
                        deletedCallsIndexes.get(i), new RobotKeywordCall[] { call }));
            }
        }
        return commands;
    }

}
