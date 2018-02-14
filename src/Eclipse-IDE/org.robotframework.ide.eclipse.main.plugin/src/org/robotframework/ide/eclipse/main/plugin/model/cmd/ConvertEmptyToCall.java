/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotEmptyLine;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class ConvertEmptyToCall extends EditorCommand {

    private final RobotEmptyLine empty;

    private final String name;

    private RobotKeywordCall newCall;

    public ConvertEmptyToCall(final IEventBroker eventBroker, final RobotEmptyLine empty, final String name) {
        this.eventBroker = eventBroker;
        this.empty = empty;
        this.name = name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) empty.getParent();

        final int index = empty.getIndex();
        parent.removeChild(empty);
        newCall = parent.createKeywordCall(index, name, new ArrayList<>(), "");

        RedEventBroker.using(eventBroker).additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(newCall).send(
                RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new ReplaceRobotKeywordCallCommand(eventBroker, newCall, empty));
    }

    public RobotKeywordCall getNewCall() {
        return newCall;
    }
}
