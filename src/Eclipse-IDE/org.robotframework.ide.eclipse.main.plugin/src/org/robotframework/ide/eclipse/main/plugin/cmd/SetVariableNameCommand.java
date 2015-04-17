package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable.Type;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetVariableNameCommand extends EditorCommand {

    private final RobotVariable variable;
    private final String newName;

    public SetVariableNameCommand(final RobotVariable variable, final String newName) {
        this.variable = variable;
        this.newName = newName;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (newName.equals(variable.getPrefix() + variable.getName() + variable.getSuffix())) {
            return;
        }
        if (newName.startsWith(Type.LIST.getMark())) {
            if (variable.getType() == Type.SCALAR) {
                variable.setType(Type.LIST);

                eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
            }

            String nameWithoutMark = newName.substring(1);
            nameWithoutMark = nameWithoutMark.startsWith("{") ? nameWithoutMark.substring(1) : nameWithoutMark;
            nameWithoutMark = nameWithoutMark.endsWith("}") ? nameWithoutMark
                    .substring(0, nameWithoutMark.length() - 1) : nameWithoutMark;

            variable.setName(nameWithoutMark);
            eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);

        } else if (newName.startsWith(Type.SCALAR.getMark())) {
            if (variable.getType() == Type.LIST) {
                variable.setType(Type.SCALAR);

                eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
            }

            String nameWithoutMark = newName.substring(1);
            nameWithoutMark = nameWithoutMark.startsWith("{") ? nameWithoutMark.substring(1) : nameWithoutMark;
            nameWithoutMark = nameWithoutMark.endsWith("}") ? nameWithoutMark
                    .substring(0, nameWithoutMark.length() - 1) : nameWithoutMark;

            variable.setName(nameWithoutMark);
            eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);

        } else {
            variable.setName(newName);

            eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        }
    }
}
