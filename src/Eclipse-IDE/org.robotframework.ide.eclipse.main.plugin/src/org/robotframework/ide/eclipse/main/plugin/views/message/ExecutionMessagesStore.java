/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import org.eclipse.ui.services.IDisposable;

import com.google.common.base.Preconditions;

class ExecutionMessagesStore implements IDisposable {

    private final StringBuilder message = new StringBuilder();

    private boolean isOpen = false;
    private boolean isDirty = false;

    synchronized void append(final String msg) {
        // can't change store state when store is closed
        Preconditions.checkState(isOpen);

        message.append(msg);
        isDirty = true;
    }

    String getMessage() {
        return message.toString();
    }

    boolean isOpen() {
        return isOpen;
    }

    void open() {
        isOpen = true;
    }

    void close() {
        isOpen = false;
        // we'll no longer write messages, so trim the data to save some memory
        message.trimToSize();
    }

    @Override
    public synchronized void dispose() {
        message.setLength(0);
        message.trimToSize();
    }

    boolean checkDirtyAndReset() {
        final boolean wasDirty = isDirty;
        isDirty = false;
        return wasDirty;
    }
}