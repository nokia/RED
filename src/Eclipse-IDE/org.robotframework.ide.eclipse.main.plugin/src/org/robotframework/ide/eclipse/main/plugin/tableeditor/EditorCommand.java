/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;

public abstract class EditorCommand {

    @Inject
    protected IEventBroker eventBroker;

    protected abstract void execute() throws CommandExecutionException;

    public class CommandExecutionException extends RuntimeException {

        public CommandExecutionException(final String message) {
            super(message);
        }

        public CommandExecutionException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
