/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.NamesGenerator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class CreateFreshCaseCommand extends EditorCommand {

    private static final String DEFAULT_NAME = "case";

    private final RobotCasesSection casesSection;
    private final int index;
    private RobotCase newTestCase;

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

        if (index == -1) {
            newTestCase = casesSection.createTestCase(name);
        } else {
            newTestCase = casesSection.createTestCase(index, name);
        }

        RedEventBroker.using(eventBroker)
            .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(newTestCase)
            .send(RobotModelEvents.ROBOT_CASE_ADDED, casesSection);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new DeleteCasesCommand(newArrayList(newTestCase)));
    }
}
