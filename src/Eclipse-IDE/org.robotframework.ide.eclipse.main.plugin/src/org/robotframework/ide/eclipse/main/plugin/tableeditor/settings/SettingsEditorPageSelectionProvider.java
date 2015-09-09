/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

class SettingsEditorPageSelectionProvider implements ISelectionProvider {

    private final List<TableViewer> viewers;
    private final List<TranslatingSelectionChangeListener> listeners = newArrayList();
    private TableViewer activeViewer;

    public SettingsEditorPageSelectionProvider(final TableViewer... viewers) {
        this.viewers = newArrayList(Iterables.filter(Arrays.asList(viewers), Predicates.notNull()));

        for (final TableViewer viewer : this.viewers) {
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
        final TranslatingSelectionChangeListener wrappingListener = new TranslatingSelectionChangeListener(listener);
        listeners.add(wrappingListener);

        for (final TableViewer viewer : viewers) {
            viewer.addSelectionChangedListener(wrappingListener);
            viewer.getControl().addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(final DisposeEvent e) {
                    listeners.remove(wrappingListener);
                }
            });
        }
    }

    @Override
    public ISelection getSelection() {
        if (activeViewer == null) {
            return StructuredSelection.EMPTY;
        }
        return translateEntriesToSettings(activeViewer.getSelection());
    }

    @SuppressWarnings("rawtypes")
    private ISelection translateEntriesToSettings(final ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            final int size = structuredSelection.size();
            final List<Entry> entries = Selections.getElements(structuredSelection, Entry.class);

            if (!entries.isEmpty() && entries.size() == size) {
                return new StructuredSelection(newArrayList(Iterables.filter(
                        Iterables.transform(entries,
                        new Function<Entry, RobotElement>() {
                            @Override
                            public RobotElement apply(final Entry entry) {
                                return (RobotElement) entry.getValue();
                            }
                        }), Predicates.notNull())));
            }
            return selection;
        }
        return selection;
    }

    @Override
    public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
        TranslatingSelectionChangeListener foundListener = null;
        for (final TranslatingSelectionChangeListener wrappingListener : listeners) {
            if (wrappingListener.wrappedListener == listener) {
                foundListener = wrappingListener;
                break;
            }
        }
        if (foundListener == null) {
            return;
        }

        for (final TableViewer viewer : viewers) {
            viewer.removeSelectionChangedListener(foundListener);
        }
    }

    @Override
    public void setSelection(final ISelection selection) {
        if (activeViewer != null) {
            activeViewer.setSelection(selection);
        }
    }

    private class TranslatingSelectionChangeListener implements ISelectionChangedListener {

        private final ISelectionChangedListener wrappedListener;

        private TranslatingSelectionChangeListener(final ISelectionChangedListener listener) {
            wrappedListener = listener;
        }

        @Override
        public void selectionChanged(final SelectionChangedEvent event) {
            wrappedListener.selectionChanged(new SelectionChangedEvent(event.getSelectionProvider(),
                    translateEntriesToSettings(event.getSelection())));
        }
    }
}
