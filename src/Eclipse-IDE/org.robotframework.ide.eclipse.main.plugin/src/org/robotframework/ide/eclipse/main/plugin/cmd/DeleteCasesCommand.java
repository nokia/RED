package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteCasesCommand extends EditorCommand {

    private final List<RobotCase> casesToDelete;

    public DeleteCasesCommand(final List<RobotCase> casesToDelete) {
        this.casesToDelete = casesToDelete;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (casesToDelete.isEmpty()) {
            return;
        }
        final RobotSuiteFileSection casesSection = (RobotSuiteFileSection) casesToDelete.get(0).getParent();
        casesSection.getChildren().removeAll(casesToDelete);

        eventBroker.post(RobotModelEvents.ROBOT_CASE_REMOVED, casesSection);
    }
}
