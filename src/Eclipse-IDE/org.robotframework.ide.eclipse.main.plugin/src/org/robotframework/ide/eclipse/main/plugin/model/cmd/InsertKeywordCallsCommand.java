/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Arrays;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class InsertKeywordCallsCommand extends EditorCommand {

    private final IRobotCodeHoldingElement parent;
    private final int index;
    private final List<RobotKeywordCall> callsToInsert;

    public InsertKeywordCallsCommand(final IRobotCodeHoldingElement parent, final RobotKeywordCall[] callsToInsert) {
        this(parent, -1, callsToInsert);
    }

    public InsertKeywordCallsCommand(final IRobotCodeHoldingElement parent, final int index,
            final RobotKeywordCall[] callsToInsert) {
        this.parent = parent;
        this.index = index;
        this.callsToInsert = Arrays.asList(callsToInsert);
    }

    @Override
    public void execute() throws CommandExecutionException {
        for (final RobotKeywordCall call : callsToInsert) {
            call.setParent(parent);
        }
        if (index == -1) {
            parent.getChildren().addAll(callsToInsert);
        } else {
            parent.getChildren().addAll(index, callsToInsert);
        }

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
    }
}
