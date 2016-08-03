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

    private final int codeHoldingElementIndex;

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement parent) {
        this(parent, parent.getChildren().size());
    }

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement parent, final int codeHoldingElementIndex) {
        this.parent = parent;
        this.codeHoldingElementIndex = codeHoldingElementIndex;
    }

    @Override
    public void execute() throws CommandExecutionException {

        parent.createKeywordCall(codeHoldingElementIndex, "", new ArrayList<String>(), "");

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
    }
}
