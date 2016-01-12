/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

public class ViewersCombiningSelectionProviderTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test(expected = NullPointerException.class)
    public void npeIsThrown_whenTryingToCreateProviderFromNullArray() {
        final TableViewer[] viewers = null;
        new ViewersCombiningSelectionProvider(viewers);
    }

    @Test
    public void selectionListenerIsInformed_whenSelectionInAnyViewerChanges() {
        final TableViewer viewer1 = createViewer(shellProvider.getShell(), "viewer1_a", "viewer1_b");
        final TableViewer viewer2 = createViewer(shellProvider.getShell(), "viewer2_x", "viewer2_y");
        final ViewersCombiningSelectionProvider provider = new ViewersCombiningSelectionProvider(viewer1, viewer2);

        final List<String> allSelected = new ArrayList<>();
        final ISelectionChangedListener listener = new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final List<String> selected = Selections.getElements((IStructuredSelection) event.getSelection(), String.class);
                allSelected.addAll(selected);
            }
        };
        provider.addSelectionChangedListener(listener);

        viewer1.setSelection(newSelection("viewer1_a"));
        viewer2.setSelection(newSelection("viewer2_y"));

        provider.removeSelectionChangedListener(listener);

        viewer1.setSelection(newSelection("viewer1_b"));
        viewer2.setSelection(newSelection("viewer2_x"));

        assertThat(allSelected).containsExactly("viewer1_a", "viewer2_y");
    }

    @Test
    public void whenSelectionIsSet_itIsChangedInActiveViewer() {
        final TableViewer viewer1 = createViewer(shellProvider.getShell(), "viewer1_a", "viewer1_b");
        final TableViewer viewer2 = createViewer(shellProvider.getShell(), "viewer2_x", "viewer2_y");
        final ViewersCombiningSelectionProvider provider = new ViewersCombiningSelectionProvider(viewer1, viewer2);

        provider.setSelection(newSelection("viewer1_a"));
        provider.setSelection(newSelection("viewer2_x"));
        assertThat(viewer1.getSelection()).isEqualTo(StructuredSelection.EMPTY);
        assertThat(viewer2.getSelection()).isEqualTo(StructuredSelection.EMPTY);

        viewer1.getTable().notifyListeners(SWT.FocusIn, new Event());

        provider.setSelection(newSelection("viewer1_a"));
        assertThat(viewer1.getSelection()).isEqualTo(newSelection("viewer1_a"));
        assertThat(viewer2.getSelection()).isEqualTo(StructuredSelection.EMPTY);
    }

    @Test
    public void whenThereIsNoActiveViewer_selectionIsEmpty() {
        final TableViewer viewer1 = createViewer(shellProvider.getShell(), "viewer1_a", "viewer1_b");
        final TableViewer viewer2 = createViewer(shellProvider.getShell(), "viewer2_x", "viewer2_y");
        final ViewersCombiningSelectionProvider provider = new ViewersCombiningSelectionProvider(viewer1, viewer2);

        viewer1.setSelection(newSelection());
        viewer2.setSelection(newSelection("viewer2_y"));

        assertThat(viewer1.getTable().isFocusControl()).isFalse();
        assertThat(viewer2.getTable().isFocusControl()).isFalse();

        assertThat(provider.getSelection()).isEqualTo(StructuredSelection.EMPTY);
    }

    @Test
    public void selectionFromFocusedViewerIsReturned_1() {
        final TableViewer viewer1 = createViewer(shellProvider.getShell(), "viewer1_a", "viewer1_b");
        final TableViewer viewer2 = createViewer(shellProvider.getShell(), "viewer2_x", "viewer2_y");
        final ViewersCombiningSelectionProvider provider = new ViewersCombiningSelectionProvider(viewer1, viewer2);

        viewer1.setSelection(newSelection("viewer1_a"));
        viewer2.setSelection(newSelection("viewer2_y"));

        viewer1.getTable().notifyListeners(SWT.FocusIn, new Event());
        
        assertThat(provider.getSelection()).isEqualTo(newSelection("viewer1_a"));
    }

    @Test
    public void selectionFromFocusedViewerIsReturned_2() {
        final TableViewer viewer1 = createViewer(shellProvider.getShell(), "viewer1_a", "viewer1_b");
        final TableViewer viewer2 = createViewer(shellProvider.getShell(), "viewer2_x", "viewer2_y");
        final ViewersCombiningSelectionProvider provider = new ViewersCombiningSelectionProvider(viewer1, viewer2);

        viewer1.setSelection(newSelection("viewer1_a"));
        viewer2.setSelection(newSelection("viewer2_y"));

        viewer2.getTable().notifyListeners(SWT.FocusIn, new Event());

        assertThat(provider.getSelection()).isEqualTo(newSelection("viewer2_y"));
    }

    private static StructuredSelection newSelection(final String... elements) {
        return new StructuredSelection(elements);
    }

    private TableViewer createViewer(final Composite parent, final String... inputElements) {
        final TableViewer viewer = new TableViewer(parent);
        viewer.setContentProvider(new StructuredContentProvider() {

            @Override
            public Object[] getElements(final Object inputElement) {
                return (Object[]) inputElement;
            }
        });
        viewer.setInput(inputElements);
        return viewer;
    }

}
