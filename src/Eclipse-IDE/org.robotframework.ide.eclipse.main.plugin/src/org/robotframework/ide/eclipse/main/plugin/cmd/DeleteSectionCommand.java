package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteSectionCommand extends EditorCommand {

    private final List<RobotSuiteFileSection> sectionsToDelete;

    public DeleteSectionCommand(final List<RobotSuiteFileSection> sections) {
        this.sectionsToDelete = sections;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (sectionsToDelete.isEmpty()) {
            return;
        }
        final RobotSuiteFile suiteFile = sectionsToDelete.get(0).getSuiteFile();
        suiteFile.getChildren().removeAll(sectionsToDelete);

        eventBroker.post(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED, suiteFile);
    }
}
