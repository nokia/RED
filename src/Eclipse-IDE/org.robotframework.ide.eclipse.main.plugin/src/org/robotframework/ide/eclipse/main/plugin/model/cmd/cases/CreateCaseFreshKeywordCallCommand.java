/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import java.util.ArrayList;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateCaseFreshKeywordCallCommand extends EditorCommand {

    private final RobotCase parent;

    private final int index;

    public CreateCaseFreshKeywordCallCommand(final RobotCase parent) {
        this(parent, parent.getChildren().size());
    }

    public CreateCaseFreshKeywordCallCommand(final RobotCase parent, final int index) {
        this.parent = parent;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        parent.createKeywordCall(index, "", new ArrayList<String>(), "");

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
    }
}
