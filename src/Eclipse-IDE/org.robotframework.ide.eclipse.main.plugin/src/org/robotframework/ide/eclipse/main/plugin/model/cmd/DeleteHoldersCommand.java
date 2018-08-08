/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteHoldersCommand extends EditorCommand {

    private final List<? extends RobotCodeHoldingElement<?>> elementsToDelete;

    private final List<Integer> deletedElementsIndexes = new ArrayList<>();

    public DeleteHoldersCommand(final List<? extends RobotCodeHoldingElement<?>> holdersToDelete) {
        this.elementsToDelete = holdersToDelete;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (elementsToDelete.isEmpty()) {
            return;
        }
        for (final RobotCodeHoldingElement<?> elementToDelete : elementsToDelete) {
            deletedElementsIndexes.add(elementToDelete.getIndex());
        }
        final RobotSuiteFileSection casesSection = elementsToDelete.get(0).getParent();
        casesSection.removeChildren(elementsToDelete);

        eventBroker.send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, casesSection);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(setupUndoCommandsForDeletedCases());
    }

    private List<EditorCommand> setupUndoCommandsForDeletedCases() {
        final List<EditorCommand> commands = new ArrayList<>();
        if (elementsToDelete.size() == deletedElementsIndexes.size()) {
            for (int i = 0; i < elementsToDelete.size(); i++) {
                final RobotCodeHoldingElement<?> holder = elementsToDelete.get(i);
                commands.add(new InsertHoldersCommand(holder.getParent(), deletedElementsIndexes.get(i),
                        new RobotCodeHoldingElement<?>[] { holder }));
            }
        }
        return commands;
    }
}
