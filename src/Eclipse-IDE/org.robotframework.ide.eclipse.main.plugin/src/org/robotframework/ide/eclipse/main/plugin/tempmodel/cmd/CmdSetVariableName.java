package org.robotframework.ide.eclipse.main.plugin.tempmodel.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotVariable;

public class CmdSetVariableName {

    private final RobotVariable variable;
    private final String newName;

    public CmdSetVariableName(final RobotVariable variable, final String newName) {
        this.variable = variable;
        this.newName = newName;
    }

    public void execute() {
    }

    public void undo() {
    }
}
