package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.Collections;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveCaseDownCommand extends EditorCommand {

    private final RobotCasesSection casesSection;
    private final int index;

    public MoveCaseDownCommand(final RobotCasesSection casesSection, final int index) {
        this.casesSection = casesSection;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final List<RobotElement> children = casesSection.getChildren();
        if (index == children.size() - 1) {
            return;
        }
        Collections.swap(children, index, index + 1);

        eventBroker.post(RobotModelEvents.ROBOT_CASE_MOVED, casesSection);
    }

}
