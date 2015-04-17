package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
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
        variable.setValue(newValue);
        
        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }
}
