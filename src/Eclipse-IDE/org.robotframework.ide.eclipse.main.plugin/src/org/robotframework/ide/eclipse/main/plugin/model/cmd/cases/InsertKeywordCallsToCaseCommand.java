/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import java.util.Arrays;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class InsertKeywordCallsToCaseCommand extends EditorCommand {

    private final RobotCase parent;

    private final int index;
    private final List<RobotKeywordCall> callsToInsert;

    public InsertKeywordCallsToCaseCommand(final RobotCase parent, final RobotKeywordCall[] callsToInsert) {
        this(parent, -1, callsToInsert);
    }

    public InsertKeywordCallsToCaseCommand(final RobotCase parent, final int index,
            final RobotKeywordCall[] callsToInsert) {
        this.parent = parent;
        this.index = index;
        this.callsToInsert = Arrays.asList(callsToInsert);
    }

    @Override
    public void execute() throws CommandExecutionException {

        // FIXME : settings should be inserted somewhere at the begining; while execution rows after
        // the settings

        int shift = 0;
        for (final RobotKeywordCall call : callsToInsert) {
            parent.insertKeywordCall(index < 0 ? -1 : index + shift, call);
            shift++;
        }
 
        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
    }
}
