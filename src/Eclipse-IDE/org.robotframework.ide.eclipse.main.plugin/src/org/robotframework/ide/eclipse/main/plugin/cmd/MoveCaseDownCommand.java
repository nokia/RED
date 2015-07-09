package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.Collections;

import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveCaseDownCommand extends EditorCommand {

    private final RobotCase testCase;

    public MoveCaseDownCommand(final RobotCase testCase) {
        this.testCase = testCase;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotElement section = testCase.getParent();
        final int size = section.getChildren().size();
        final int index = section.getChildren().indexOf(testCase);
        if (index == size - 1) {
            return;
        }
        Collections.swap(section.getChildren(), index, index + 1);

        eventBroker.post(RobotModelEvents.ROBOT_CASE_MOVED, section);
    }

}
