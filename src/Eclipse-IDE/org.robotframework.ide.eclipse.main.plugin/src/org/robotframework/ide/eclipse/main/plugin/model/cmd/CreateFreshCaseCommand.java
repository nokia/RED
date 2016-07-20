/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshCaseCommand extends EditorCommand {

    private static final String DEFAULT_NAME = "case";
    private final RobotCasesSection casesSection;
    private final int index;
    private final boolean notifySync;

    public CreateFreshCaseCommand(final RobotCasesSection casesSection, final boolean notifySync) {
        this(casesSection, -1, notifySync);
    }

    public CreateFreshCaseCommand(final RobotCasesSection casesSection, final int index) {
        this(casesSection, index, false);
    }

    private CreateFreshCaseCommand(final RobotCasesSection casesSection, final int index, final boolean notifySync) {
        this.casesSection = casesSection;
        this.index = index;
        this.notifySync = notifySync;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final String name = NamesGenerator.generateUniqueName(casesSection, DEFAULT_NAME);

        if (index == -1) {
            casesSection.createTestCase(name);
        } else {
            casesSection.createTestCase(index, name);
        }

        if (notifySync) {
            eventBroker.send(RobotModelEvents.ROBOT_CASE_ADDED, casesSection);
        } else {
            eventBroker.post(RobotModelEvents.ROBOT_CASE_ADDED, casesSection);
        }
    }
}
