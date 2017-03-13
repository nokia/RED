/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.services.IDisposable;
import org.rf.ide.core.execution.ExecutionElement;

class ExecutionElementsStore implements IDisposable {

    private final List<ExecutionElement> elements = new ArrayList<>();

    private final List<ExecutionElementsStoreListener> listeners = new ArrayList<>();

    void addStoreListener(final ExecutionElementsStoreListener listener) {
        listeners.add(listener);
    }

    void removeStoreListener(final ExecutionElementsStoreListener storeListener) {
        listeners.remove(storeListener);
    }

    @Override
    public void dispose() {
        elements.clear();
        listeners.clear();
    }

    void addElement(final ExecutionElement element) {
        elements.add(element);
        listeners.forEach(listener -> listener.storeChanged(this, element));
    }

    List<ExecutionElement> getElements() {
        return elements;
    }

    @FunctionalInterface
    static interface ExecutionElementsStoreListener {

        void storeChanged(ExecutionElementsStore store, ExecutionElement element);
    }
}
