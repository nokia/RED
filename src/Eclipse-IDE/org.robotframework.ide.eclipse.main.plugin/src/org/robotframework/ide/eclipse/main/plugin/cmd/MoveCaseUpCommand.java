package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.Collections;

import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveCaseUpCommand extends EditorCommand {

    private final RobotCase testCase;

    public MoveCaseUpCommand(final RobotCase testCase) {
        this.testCase = testCase;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotElement section = testCase.getParent();
        final int index = section.getChildren().indexOf(testCase);
        if (index == 0) {
            return;
        }
        Collections.swap(section.getChildren(), index, index - 1);

        eventBroker.post(RobotModelEvents.ROBOT_CASE_MOVED, section);
    }

}
