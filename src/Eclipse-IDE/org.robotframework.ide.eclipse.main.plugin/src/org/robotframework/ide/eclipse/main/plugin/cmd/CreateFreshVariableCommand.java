package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable.Type;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshVariableCommand extends EditorCommand {

    private static final String DEFAULT_NAME = "var";
    private final RobotSuiteFileSection variablesSection;
    private final int index;
    private final boolean notifySync;
    private final Type variableType;

    public CreateFreshVariableCommand(final RobotSuiteFileSection variablesSection, final boolean notifySynchronously) {
        this(variablesSection, -1, notifySynchronously, Type.SCALAR);
    }

    public CreateFreshVariableCommand(final RobotSuiteFileSection variablesSection, final int index, Type variableType) {
        this(variablesSection, index, false, variableType);
    }

    private CreateFreshVariableCommand(final RobotSuiteFileSection variablesSection, final int index,
            final boolean notifySynchronously, Type variableType) {
        this.variablesSection = variablesSection;
        this.index = index;
        this.notifySync = notifySynchronously;
        this.variableType = variableType;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final String name = NamesGenerator.generateUniqueName(variablesSection, DEFAULT_NAME);

        final RobotVariable variable = new RobotVariable(variablesSection, variableType, name, "", "");

        if (index == -1) {
            variablesSection.getChildren().add(variable);
        } else {
            variablesSection.getChildren().add(index, variable);
        }

        if (notifySync) {
            eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_ADDED, variablesSection);
        } else {
            eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_ADDED, variablesSection);
        }
    }
}
