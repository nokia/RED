/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import java.util.Optional;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.services.IDisposable;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

import com.google.common.base.Preconditions;

class ExecutionMessagesStore implements IDisposable {

    private Optional<Integer> limit;
    private IPreferenceChangeListener preferenceListener;
    
    private final StringBuilder message = new StringBuilder();

    private boolean isOpen = false;
    private boolean isDirty = false;

    public ExecutionMessagesStore() {
        setLimit(RedPlugin.getDefault().getPreferences().getMessageLogViewLimit());
        this.preferenceListener = event -> {
            if (event == null) {
                return;
            } else if (RedPreferences.LIMIT_MSG_LOG_OUTPUT.equals(event.getKey())) {
                setLimit(RedPlugin.getDefault().getPreferences().getMessageLogViewLimit());

            } else if (RedPreferences.LIMIT_MSG_LOG_LENGTH.equals(event.getKey())) {
                setLimit(RedPlugin.getDefault().getPreferences().getMessageLogViewLimit());
            }
        };
        InstanceScope.INSTANCE.getNode(RedPlugin.PLUGIN_ID).addPreferenceChangeListener(preferenceListener);
    }

    synchronized void setLimit(final Optional<Integer> limit) {
        this.limit = limit;
    }

    synchronized Optional<Integer> getLimit() {
        return limit;
    }

    synchronized void append(final String msg) {
        // can't change store state when store is closed
        Preconditions.checkState(isOpen);

        message.append(msg);
        isDirty = true;

        getLimit().map(limit -> message.length() - limit).filter(i -> i >= 0).ifPresent(end -> message.delete(0, end));
    }

    void clear() {
        message.setLength(0);
        message.trimToSize();
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
        clear();
        InstanceScope.INSTANCE.getNode(RedPlugin.PLUGIN_ID).removePreferenceChangeListener(preferenceListener);
    }

    boolean checkDirtyAndReset() {
        final boolean wasDirty = isDirty;
        isDirty = false;
        return wasDirty;
    }
}