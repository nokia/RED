package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable.Type;
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

        // it has to be send, not posted
        // otherwise it is not possible to traverse between cells, because the cell
        // is traversed and then main thread has to handle incoming posted event which
        // closes currently active cell editor
        if (newName.startsWith(Type.LIST.getMark())) {
            if (variable.getType() != Type.LIST) {
                variable.setType(Type.LIST);

                eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
            }
            variable.setName(getNameWithoutMarks());
            eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        } else if (newName.startsWith(Type.SCALAR.getMark())) {
            if (variable.getType() != Type.SCALAR) {
                variable.setType(Type.SCALAR);

                eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
            }
            variable.setName(getNameWithoutMarks());
            eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        } else if (newName.startsWith(Type.DICTIONARY.getMark())) {
            if (variable.getType() != Type.DICTIONARY) {
                variable.setType(Type.DICTIONARY);

                eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
            }
            variable.setName(getNameWithoutMarks());
            eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        } else {
            variable.setName(newName);

            eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        }
    }

    private String getNameWithoutMarks() {
        String nameWithoutMark = newName.substring(1);
        nameWithoutMark = nameWithoutMark.startsWith("{") ? nameWithoutMark.substring(1) : nameWithoutMark;
        nameWithoutMark = nameWithoutMark.endsWith("}") ? nameWithoutMark
                .substring(0, nameWithoutMark.length() - 1) : nameWithoutMark;
        return nameWithoutMark;
    }
}
