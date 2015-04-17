package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.Collections;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveVariableDownCommand extends EditorCommand {

    private final RobotSuiteFileSection variablesSection;
    private final int index;

    public MoveVariableDownCommand(final RobotSuiteFileSection variablesSection, final int index) {
        this.variablesSection = variablesSection;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final List<RobotElement> children = variablesSection.getChildren();
        if (index == children.size() - 1) {
            return;
        }
        Collections.swap(children, index, index + 1);

        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_MOVED, variablesSection);
    }
}
