/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveCodeHolderUpCommand extends EditorCommand {

    private final RobotCodeHoldingElement<?> holder;
    private boolean wasMoved = true;
    

    public MoveCodeHolderUpCommand(final RobotCodeHoldingElement<?> holder) {
        this.holder = holder;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotSuiteFileSection section = holder.getParent();
        final int index = section.getChildren().indexOf(holder);
        if (index == 0) {
            wasMoved = false;
            return;
        }
        Collections.swap(section.getChildren(), index, index - 1);

        final ARobotSectionTable linkedElement = section.getLinkedElement();
        linkedElement.moveUpElement(holder.getLinkedElement());

        eventBroker.send(RobotModelEvents.ROBOT_ELEMENT_MOVED, section);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(wasMoved ? new MoveCodeHolderDownCommand(holder) : new EmptyCommand());
    }
}
