/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.nattable;

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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public class SettingsEditorPageSelectionProvider implements ISelectionProvider {
    
    private List<ISettingsFormFragment> formFragments;

    private ISettingsFormFragment activeFormFragment;
    
    private final List<TranslatingSelectionChangeListener> listeners = newArrayList();
    
    public SettingsEditorPageSelectionProvider(final ISettingsFormFragment... formFragments) {
        this.formFragments = newArrayList(Iterables.filter(Arrays.asList(formFragments), Predicates.notNull()));
        for (final ISettingsFormFragment formFragment : formFragments) {
            addFocusListener(formFragment);
        }
    }

    private void addFocusListener(final ISettingsFormFragment formFragment) {
        if(formFragment.getTable() != null) {
            formFragment.getTable().addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(final FocusEvent e) {
                    activeFormFragment = formFragment;
                }
            });
        }
    }

    @Override
    public void addSelectionChangedListener(final ISelectionChangedListener listener) {
        final TranslatingSelectionChangeListener wrappingListener = new TranslatingSelectionChangeListener(listener);
        listeners.add(wrappingListener);
        
        for (final ISettingsFormFragment formFragment : formFragments) {
            if(formFragment.getTable() != null && formFragment.getSelectionProvider() != null) {
                formFragment.getSelectionProvider().addSelectionChangedListener(wrappingListener);
                formFragment.getTable().addDisposeListener(new DisposeListener() {

                    @Override
                    public void widgetDisposed(DisposeEvent e) {
                        listeners.remove(wrappingListener);
                    }
                });
            }
        }
    }

    @Override
    public ISelection getSelection() {
        if (activeFormFragment == null || activeFormFragment.getSelectionProvider() == null) {
            return StructuredSelection.EMPTY;
        }
        return translateEntriesToSettings(activeFormFragment.getSelectionProvider().getSelection());
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
        
        for (final ISettingsFormFragment formFragment : formFragments) {
            if(formFragment.getSelectionProvider() != null) {
                formFragment.getSelectionProvider().removeSelectionChangedListener(foundListener);
            }
        }
    }

    @Override
    public void setSelection(final ISelection selection) {
        if (activeFormFragment != null && activeFormFragment.getSelectionProvider() != null) {
            activeFormFragment.getSelectionProvider().setSelection(selection);
        }
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
