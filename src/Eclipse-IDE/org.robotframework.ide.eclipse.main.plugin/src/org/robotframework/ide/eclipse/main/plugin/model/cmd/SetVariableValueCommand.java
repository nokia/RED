/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetVariableValueCommand extends EditorCommand {

    private final RobotVariable variable;
    private final String newValue;

    public SetVariableValueCommand(final RobotVariable variable, final String newValue) {
        this.variable = variable;
        this.newValue = newValue;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (variable.getValue().equals(newValue)) {
            return;
        }
        // variable.setValue(newValue);
        // final Position position = variable.getPositionOfValue();
        // try {
        // document.replace(position.getOffset(), position.getLength(), newValue);
        // } catch (final BadLocationException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // it has to be send, not posted
        // otherwise it is not possible to traverse between cells, because the cell
        // is traversed and then main thread has to handle incoming posted event which
        // closes currently active cell editor
        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }
}
