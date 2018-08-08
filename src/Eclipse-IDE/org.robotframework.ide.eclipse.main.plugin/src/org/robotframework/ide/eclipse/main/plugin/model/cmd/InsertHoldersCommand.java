/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Arrays;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class InsertHoldersCommand extends EditorCommand {

    private final RobotSuiteFileSection holdersSection;
    private final int index;
    private final List<RobotCodeHoldingElement<?>> holdersToInsert;

    public InsertHoldersCommand(final RobotSuiteFileSection casesSection,
            final RobotCodeHoldingElement<?>[] holdersToInsert) {
        this(casesSection, -1, holdersToInsert);
    }

    public InsertHoldersCommand(final RobotSuiteFileSection casesSection, final int index,
            final RobotCodeHoldingElement<?>[] holdersToInsert) {
        this.holdersSection = casesSection;
        this.index = index;
        this.holdersToInsert = Arrays.asList(holdersToInsert);
    }

    @Override
    public void execute() throws CommandExecutionException {
        int counter = index;
        for (final RobotCodeHoldingElement<?> holder : holdersToInsert) {
            if (nameChangeIsRequired(holder)) {
                final String newName = NamesGenerator.generateUniqueName(holdersSection, holder.getName());
                holder.getLinkedElement().getName().setText(newName);
            }

            holdersSection.insertChild(counter, holder);
            counter += counter == -1 ? 0 : 1;
        }

        if (!holdersToInsert.isEmpty()) {
            RedEventBroker.using(eventBroker)
                    .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                    .to(holdersToInsert)
                    .send(RobotModelEvents.ROBOT_ELEMENT_ADDED, holdersSection);
        }
    }

    private boolean nameChangeIsRequired(final RobotCodeHoldingElement<?> holder) {
        return holdersSection.getChildren()
                .stream()
                .anyMatch(elem -> elem.getName().equalsIgnoreCase(holder.getName()));
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new DeleteHoldersCommand(holdersToInsert));
    }
}
