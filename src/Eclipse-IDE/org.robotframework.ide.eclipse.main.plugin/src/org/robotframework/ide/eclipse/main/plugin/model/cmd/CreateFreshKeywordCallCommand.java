/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshKeywordCallCommand extends EditorCommand {

    private final RobotCodeHoldingElement parent;
    private final int index;
    private final boolean notifySync;

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement parent, final boolean notifySync) {
        this(parent, -1, notifySync);
    }

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement parent, final int index, final boolean notifySync) {
        this.parent = parent;
        this.index = index;
        this.notifySync = notifySync;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (index == -1) {
            parent.createKeywordCall("", new ArrayList<String>(), "");
        } else {
            parent.createKeywordCall(index, "", new ArrayList<String>(), "");
        }

        if (notifySync) {
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
        } else {
            eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
        }
    }
}
