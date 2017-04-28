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

    void addStoreListener(final ExecutionMessagesStoreListener listener) {
        listeners.add(listener);
    }

    void removeStoreListener(final ExecutionMessagesStoreListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void dispose() {
        message.setLength(0);
        listeners.clear();
    }

    void append(final String msg) {
        message.append(msg);
        listeners.forEach(listener -> listener.storeAppended(msg));
    }

    String getMessage() {
        return message.toString();
    }

    @FunctionalInterface
    static interface ExecutionMessagesStoreListener {

        void storeAppended(String appendedMsg);
    }

}