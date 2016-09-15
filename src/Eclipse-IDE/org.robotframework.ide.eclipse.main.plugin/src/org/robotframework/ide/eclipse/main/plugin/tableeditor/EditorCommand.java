/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.EmptyCommand;

public abstract class EditorCommand {

    @Inject
    protected IEventBroker eventBroker;

    public void setEventBroker(final IEventBroker eventBroker) {
        this.eventBroker = eventBroker;
    }

    public abstract void execute() throws CommandExecutionException;

    public EditorCommand getUndoCommand() {
        return new EmptyCommand();
    }

    protected EditorCommand newUndoCommand(final EditorCommand newUndoCommand) {
        newUndoCommand.eventBroker = this.eventBroker;
        return newUndoCommand;
    }

    protected EditorCommand newUndoCompoundCommand(final CompoundEditorCommand newUndoCommand) {
        for (final EditorCommand command : newUndoCommand.getCommands()) {
            command.eventBroker = this.eventBroker;
        }
        return newUndoCommand;
    }

    public class CommandExecutionException extends RuntimeException {

        public CommandExecutionException(final String message) {
            super(message);
        }

        public CommandExecutionException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

}
