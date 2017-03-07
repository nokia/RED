/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.services.IDisposable;

class ExecutionMessagesStore implements IDisposable {

    private final StringBuilder message = new StringBuilder();

    private final List<ExecutionMessagesStoreListener> listeners = new ArrayList<>();

    synchronized void addStoreListener(final ExecutionMessagesStoreListener listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized void dispose() {
        message.setLength(0);
        listeners.clear();
    }

    synchronized void append(final String msg) {
        message.append(msg);
        listeners.stream().forEach(listener -> listener.storeAppended(this, msg));
    }

    synchronized public String getMessage() {
        return message.toString();
    }

    @FunctionalInterface
    static interface ExecutionMessagesStoreListener {

        void storeAppended(ExecutionMessagesStore store, String appendedMsg);
    }
}