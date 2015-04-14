package org.robotframework.ide.eclipse.main.plugin.tempmodel.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateSectionCommand extends EditorCommand {

    private final RobotSuiteFile suite;
    private final String sectionName;

    public CreateSectionCommand(final RobotSuiteFile suite, final String sectionName) {
        this.suite = suite;
        this.sectionName = sectionName;
    }

    @Override
    public void execute() throws CommandExecutionException {
        suite.createRobotSection(sectionName);
        
        eventBroker.post(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED, suite);
    }
}
