/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import java.util.Arrays;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class InsertCasesCommand extends EditorCommand {

    private final RobotCasesSection casesSection;
    private final int index;
    private final List<RobotCase> casesToInsert;

    public InsertCasesCommand(final RobotCasesSection casesSection, final RobotCase[] casesToInsert) {
        this(casesSection, -1, casesToInsert);
    }

    public InsertCasesCommand(final RobotCasesSection casesSection, final int index, final RobotCase[] casesToInsert) {
        this.casesSection = casesSection;
        this.index = index;
        this.casesToInsert = Arrays.asList(casesToInsert);
    }

    @Override
    public void execute() throws CommandExecutionException {
        for (final RobotCase testCase : casesToInsert) {
            testCase.setParent(casesSection);
        }
        if (index == -1) {
            casesSection.getChildren().addAll(casesToInsert);
        } else {
            casesSection.getChildren().addAll(index, casesToInsert);
        }

        eventBroker.post(RobotModelEvents.ROBOT_CASE_ADDED, casesSection);
    }
}
