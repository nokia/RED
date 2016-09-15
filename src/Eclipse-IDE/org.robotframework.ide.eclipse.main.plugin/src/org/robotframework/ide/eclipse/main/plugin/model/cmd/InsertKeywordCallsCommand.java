/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Arrays;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class InsertKeywordCallsCommand extends EditorCommand {

    private final IRobotCodeHoldingElement parent;

    private final int index;

    private final List<RobotKeywordCall> callsToInsert;

    public InsertKeywordCallsCommand(final IRobotCodeHoldingElement parent, final RobotKeywordCall[] callsToInsert) {
        this(parent, parent.getChildren().size(), callsToInsert);
    }

    public InsertKeywordCallsCommand(final IRobotCodeHoldingElement parent, final int index,
            final RobotKeywordCall[] callsToInsert) {
        this.parent = parent;
        this.index = index;
        this.callsToInsert = Arrays.asList(callsToInsert);
    }

    @Override
    public void execute() throws CommandExecutionException {
        // FIXME : we currently keep all settings before executable rows, so this condition
        // should be also fulfilled after command is executed

        final RobotCodeHoldingElement<?> parentElement = (RobotCodeHoldingElement<?>) parent;
        int shift = 0;
        for (final RobotKeywordCall call : callsToInsert) {
            parentElement.insertKeywordCall(index + shift, call);
            shift++;
        }

        RedEventBroker.using(eventBroker)
            .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(callsToInsert)
            .send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
    }
    
    @Override
    public EditorCommand getUndoCommand() {
        return newUndoCommand(new DeleteKeywordCallCommand(callsToInsert));
    }
}
