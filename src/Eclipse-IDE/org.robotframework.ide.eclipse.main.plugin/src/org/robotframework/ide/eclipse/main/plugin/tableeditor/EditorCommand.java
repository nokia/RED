/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.EmptyCommand;

public abstract class EditorCommand {

    @Inject
    protected IEventBroker eventBroker;

    private EditorCommand parent;

    public void setEventBroker(final IEventBroker eventBroker) {
        this.eventBroker = eventBroker;
    }

    public abstract void execute() throws CommandExecutionException;

    public List<EditorCommand> getUndoCommands() {
        final List<EditorCommand> commands = newArrayList();
        commands.add(new EmptyCommand());
        return commands;
    }
    
    public EditorCommand getParent() {
        return parent;
    }

    protected List<EditorCommand> newUndoCommands(final EditorCommand newUndoCommand) {
        return newUndoCommands(newArrayList(newUndoCommand));
    }

    protected List<EditorCommand> newUndoCommands(final List<EditorCommand> newUndoCommands) {
        for (final EditorCommand newUndoCommand : newUndoCommands) {
            newUndoCommand.eventBroker = this.eventBroker;
            newUndoCommand.parent = parent != null ? parent : this;
        }
        return newUndoCommands;
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
