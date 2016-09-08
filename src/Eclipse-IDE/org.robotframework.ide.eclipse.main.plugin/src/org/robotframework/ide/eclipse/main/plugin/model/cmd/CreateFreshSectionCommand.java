/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshSectionCommand extends EditorCommand {

    private final RobotSuiteFile suite;
    private final String sectionName;
    private RobotSuiteFileSection createdSection;

    public CreateFreshSectionCommand(final RobotSuiteFile suite, final String sectionName) {
        this.suite = suite;
        this.sectionName = sectionName;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final boolean hasSection = alreadyHaveSuchSection();

        if (!hasSection) {
            createdSection = suite.createRobotSection(sectionName);
            eventBroker.send(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED, suite);
        }
    }

    private boolean alreadyHaveSuchSection() {
        for (final RobotSuiteFileSection section : suite.getSections()) {
            if (section.getName().equals(sectionName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        final EditorCommand undo = createdSection == null ? new EmptyCommand()
                : new DeleteSectionCommand(createdSection);
        return newUndoCommands(undo);
    }
}
