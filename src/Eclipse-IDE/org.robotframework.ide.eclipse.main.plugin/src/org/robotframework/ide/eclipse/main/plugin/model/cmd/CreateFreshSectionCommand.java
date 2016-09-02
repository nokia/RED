/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshSectionCommand extends EditorCommand {

    private final RobotSuiteFile suite;
    private final String sectionName;

    public CreateFreshSectionCommand(final RobotSuiteFile suite, final String sectionName) {
        this.suite = suite;
        this.sectionName = sectionName;
    }

    @Override
    public void execute() throws CommandExecutionException {
        suite.createRobotSection(sectionName);
        eventBroker.send(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED, suite);
    }
}
