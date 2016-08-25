/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshKeywordCallCommand extends EditorCommand {

    private final RobotCodeHoldingElement parent;

    private final String name;

    private final List<String> args;

    private final String comment;

    private final int codeHoldingElementIndex;

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement parent) {
        this(parent, parent.getChildren().size());
    }

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement parent, final int codeHoldingElementIndex) {
        this(parent, "", new ArrayList<String>(), "", codeHoldingElementIndex);
    }

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement parent, final String name,
            final List<String> args, final String comment, final int codeHoldingElementIndex) {
        this.parent = parent;
        this.name = name;
        this.args = args;
        this.comment = comment;
        this.codeHoldingElementIndex = codeHoldingElementIndex;
    }

    @Override
    public void execute() throws CommandExecutionException {

        parent.createKeywordCall(codeHoldingElementIndex, name, args, comment);

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
    }
}
