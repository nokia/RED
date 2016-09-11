/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

class SetSimpleKeywordCallArguments extends EditorCommand {

    private final RobotKeywordCall call;

    private List<String> oldArguments;

    private final List<String> newArguments;

    SetSimpleKeywordCallArguments(final IEventBroker eventBroker, final RobotKeywordCall call,
            final List<String> newArguments) {
        this.eventBroker = eventBroker;
        this.call = call;
        this.newArguments = newArguments;
    }

    @Override
    public void execute() {
        oldArguments = call.getArguments();
        if (oldArguments.equals(newArguments)) {
            return;
        }
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) call.getParent();
        parent.getModelUpdater().setArguments(call.getLinkedElement(), newArguments);
        call.resetStored();

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, call);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new SetSimpleKeywordCallArguments(eventBroker, call, oldArguments));
    }
}