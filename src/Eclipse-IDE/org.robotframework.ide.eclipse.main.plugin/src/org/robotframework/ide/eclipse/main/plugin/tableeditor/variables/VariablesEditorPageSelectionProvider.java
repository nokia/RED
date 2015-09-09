/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;

class VariablesEditorPageSelectionProvider implements ISelectionProvider {

    private final List<TableViewer> viewers;

    private TableViewer activeViewer;

    public VariablesEditorPageSelectionProvider(final TableViewer... viewers) {
        this.viewers = Arrays.asList(viewers);

        for (final TableViewer viewer : viewers) {
            addFocusListener(viewer);
        }
    }

    private void addFocusListener(final TableViewer viewer) {
        viewer.getControl().addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(final FocusEvent e) {
                activeViewer = viewer;
            }
        });
    }

    @Override
    public void addSelectionChangedListener(final ISelectionChangedListener listener) {

        for (final TableViewer viewer : viewers) {
            viewer.addSelectionChangedListener(listener);
        }
    }

    @Override
    public ISelection getSelection() {
        if (activeViewer == null) {
            return StructuredSelection.EMPTY;
        }
        return activeViewer.getSelection();
    }

    @Override
    public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
        for (final TableViewer viewer : viewers) {
            viewer.removeSelectionChangedListener(listener);
        }
    }

    @Override
    public void setSelection(final ISelection selection) {
        if (activeViewer != null) {
            activeViewer.setSelection(selection);
        }
    }

}
