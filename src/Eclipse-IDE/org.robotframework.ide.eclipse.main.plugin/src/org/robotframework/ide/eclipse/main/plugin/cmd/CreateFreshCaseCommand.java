package org.robotframework.ide.eclipse.main.plugin.cmd;

import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshCaseCommand extends EditorCommand {

    private static final String DEFAULT_NAME = "case ";
    private final RobotCasesSection casesSection;
    private final int index;

    public CreateFreshCaseCommand(final RobotCasesSection casesSection) {
        this(casesSection, -1);
    }

    public CreateFreshCaseCommand(final RobotCasesSection casesSection, final int index) {
        this.casesSection = casesSection;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final String name = NamesGenerator.generateUniqueName(casesSection, DEFAULT_NAME);

        final RobotCase testCase = new RobotCase(casesSection, name);
        if (index == -1) {
            casesSection.getChildren().add(testCase);
        } else {
            casesSection.getChildren().add(index, testCase);
        }

        eventBroker.send(RobotModelEvents.ROBOT_CASE_ADDED, casesSection);
    }
}
