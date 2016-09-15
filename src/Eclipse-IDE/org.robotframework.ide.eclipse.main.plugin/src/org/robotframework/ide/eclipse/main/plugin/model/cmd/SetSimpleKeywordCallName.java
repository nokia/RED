/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.model.AModelElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

class SetSimpleKeywordCallName extends EditorCommand {

    private final RobotKeywordCall call;

    private String oldName;

    private final String newName;

    public SetSimpleKeywordCallName(final IEventBroker eventBroker, final RobotKeywordCall call,
            final String newName) {
        this.eventBroker = eventBroker;
        this.call = call;
        this.newName = newName;
    }

    @Override
    public void execute() {
        oldName = call.isExecutable() ? call.getName() : "[" + call.getName() + "]";

        final AModelElement<?> linkedElement = call.getLinkedElement();
        linkedElement.getDeclaration().setText(newName);

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, call);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new SetSimpleKeywordCallName(eventBroker, call, oldName));
    }
}