/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import java.util.List;

import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
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
        final RobotSuiteFileSection casesSection = casesToDelete.get(0).getParent();
        casesSection.getChildren().removeAll(casesToDelete);
        
        final TestCaseTable linkedElement = (TestCaseTable) casesSection.getLinkedElement();
        for (final RobotCase caseToDelete : casesToDelete) {
            linkedElement.removeTest(caseToDelete.getLinkedElement());
        }

        eventBroker.send(RobotModelEvents.ROBOT_CASE_REMOVED, casesSection);
    }
}
