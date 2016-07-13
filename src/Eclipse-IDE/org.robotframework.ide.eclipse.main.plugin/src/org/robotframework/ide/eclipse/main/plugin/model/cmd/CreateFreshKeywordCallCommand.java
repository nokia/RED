/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshKeywordCallCommand extends EditorCommand {

    private final RobotCodeHoldingElement parent;
    private final int index;

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement parent) {
        this(parent, -1);
    }

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement parent, final int index) {
        this.parent = parent;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (index == -1) {
            parent.createKeywordCall();
        } else {
            parent.createKeywordCall(index);
        }
        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
    }
}
