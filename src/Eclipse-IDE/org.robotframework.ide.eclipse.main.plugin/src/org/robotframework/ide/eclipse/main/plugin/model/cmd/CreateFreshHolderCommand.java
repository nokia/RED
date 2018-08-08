/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class CreateFreshHolderCommand extends EditorCommand {

    private final RobotSuiteFileSection section;
    private final int index;

    private RobotCodeHoldingElement<?> newElement;

    public CreateFreshHolderCommand(final RobotSuiteFileSection section) {
        this(section, -1);
    }

    public CreateFreshHolderCommand(final RobotSuiteFileSection section, final int index) {
        this.section = section;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final String defaultName = section.getDefaultChildName();
        newElement = (RobotCodeHoldingElement<?>) section.createChild(index,
                NamesGenerator.generateUniqueName(section, defaultName));

        RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                .to(newElement)
                .send(RobotModelEvents.ROBOT_ELEMENT_ADDED, section);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new DeleteHoldersCommand(newArrayList(newElement)));
    }
}
