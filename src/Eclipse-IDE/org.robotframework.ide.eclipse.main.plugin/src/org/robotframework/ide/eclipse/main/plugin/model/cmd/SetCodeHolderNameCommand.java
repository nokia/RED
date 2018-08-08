/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetCodeHolderNameCommand extends EditorCommand {

    private final RobotCodeHoldingElement<?> holder;
    private final String name;
    private String previousName;

    public SetCodeHolderNameCommand(final RobotCodeHoldingElement<?> holder, final String name) {
        this.holder = holder;
        this.name = name == null || name.isEmpty() ? "\\" : name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        previousName = holder.getName();
        if (name.equals(previousName)) {
            return;
        }
        holder.getLinkedElement().setName(RobotToken.create(name));

        eventBroker.send(RobotModelEvents.ROBOT_ELEMENT_NAME_CHANGED, holder);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new SetCodeHolderNameCommand(holder, previousName));
    }
}
