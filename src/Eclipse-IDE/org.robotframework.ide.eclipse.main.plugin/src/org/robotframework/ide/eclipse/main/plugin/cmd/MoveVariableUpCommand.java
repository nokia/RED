package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.Collections;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveVariableUpCommand extends EditorCommand {

    private final RobotSuiteFileSection variablesSection;
    private final int index;

    public MoveVariableUpCommand(final RobotSuiteFileSection variablesSection, final int index) {
        this.variablesSection = variablesSection;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (index == 0) {
            return;
        }
        Collections.swap(variablesSection.getChildren(), index, index - 1);

        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_MOVED, variablesSection);
    }
}
