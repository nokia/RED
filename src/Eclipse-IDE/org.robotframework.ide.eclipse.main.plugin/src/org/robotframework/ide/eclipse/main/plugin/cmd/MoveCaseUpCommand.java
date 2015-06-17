package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.Collections;

import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveCaseUpCommand extends EditorCommand {

    private final RobotCasesSection casesSection;
    private final int index;

    public MoveCaseUpCommand(final RobotCasesSection casesSection, final int index) {
        this.casesSection = casesSection;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (index == 0) {
            return;
        }
        Collections.swap(casesSection.getChildren(), index, index - 1);

        eventBroker.post(RobotModelEvents.ROBOT_CASE_MOVED, casesSection);
    }

}
