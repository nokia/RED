/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import org.eclipse.ui.services.IDisposable;

class ExecutionMessagesStore implements IDisposable {

    private final StringBuilder message = new StringBuilder();

    private boolean isDirty = false;

    void append(final String msg) {
        message.append(msg);
        isDirty = true;
    }

    String getMessage() {
        return message.toString();
    }

    @Override
    public void dispose() {
        message.setLength(0);
    }

    boolean checkDirtyAndReset() {
        final boolean wasDirty = isDirty;
        isDirty = false;
        return wasDirty;
    }
}