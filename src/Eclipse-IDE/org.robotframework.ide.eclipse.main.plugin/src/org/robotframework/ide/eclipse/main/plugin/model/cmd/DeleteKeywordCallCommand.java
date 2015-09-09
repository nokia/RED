/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteKeywordCallCommand extends EditorCommand {

    private final List<? extends RobotKeywordCall> callsToDelete;

    public DeleteKeywordCallCommand(final List<? extends RobotKeywordCall> callsToDelete) {
        this.callsToDelete = callsToDelete;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (callsToDelete.isEmpty()) {
            return;
        }
        final RobotElement parent = callsToDelete.get(0).getParent();

        parent.getChildren().removeAll(callsToDelete);

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, parent);
    }
}
