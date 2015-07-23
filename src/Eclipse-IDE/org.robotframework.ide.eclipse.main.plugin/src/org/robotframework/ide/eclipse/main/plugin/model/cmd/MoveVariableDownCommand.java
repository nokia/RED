package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Collections;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveVariableDownCommand extends EditorCommand {

    private final RobotVariablesSection variablesSection;
    private final int index;

    public MoveVariableDownCommand(final RobotVariablesSection variablesSection, final int index) {
        this.variablesSection = variablesSection;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final List<RobotVariable> children = variablesSection.getChildren();
        if (index == children.size() - 1) {
            return;
        }
        Collections.swap(children, index, index + 1);

        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_MOVED, variablesSection);
    }
}
