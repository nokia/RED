/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotEmptyLine;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class ConvertCallToEmpty extends EditorCommand {

    private final RobotKeywordCall call;

    private final String emptyName;

    private RobotEmptyLine newEmpty;

    public ConvertCallToEmpty(final IEventBroker eventBroker, final RobotKeywordCall call, final String emptyName) {
        this.eventBroker = eventBroker;
        this.call = call;
        this.emptyName = emptyName;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) call.getParent();

        final int index = call.getIndex();
        parent.removeChild(call);
        newEmpty = parent.createEmpty(index, emptyName);

        RedEventBroker.using(eventBroker).additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(newEmpty).send(
                RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new ReplaceRobotKeywordCallCommand(eventBroker, newEmpty, call));
    }
}