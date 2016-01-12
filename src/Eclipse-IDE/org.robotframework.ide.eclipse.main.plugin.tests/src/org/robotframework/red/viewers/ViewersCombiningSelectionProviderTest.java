/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

@Ignore
public class ViewersCombiningSelectionProviderTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test(expected = NullPointerException.class)
    public void npeIsThrown_whenTryingToCreateProviderFromNullArray() {
        new ViewersCombiningSelectionProvider(null);
    }

    public void selectionListenersIsInformed_whenSelectionInAnyViewerChanges() {

    }

    @Test
    public void whenThereIsNoActiveViewer_selectionIsEmpty() {
        final TableViewer viewer1 = createViewer(shellProvider.getShell(), "viewer1_a", "viewer1_b");
        final TableViewer viewer2 = createViewer(shellProvider.getShell(), "viewer2_x", "viewer2_y");
        final ViewersCombiningSelectionProvider provider = new ViewersCombiningSelectionProvider(viewer1, viewer2);

        viewer1.setSelection(newSelection());
        viewer2.setSelection(new StructuredSelection(new Object[] { "viewer2_y" }));

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

        assertThat(viewer1.getTable().isFocusControl()).isFalse();
        assertThat(viewer2.getTable().isFocusControl()).isFalse();

        viewer1.getTable().forceFocus();

        assertThat(viewer1.getTable().isFocusControl()).isTrue();
        assertThat(viewer2.getTable().isFocusControl()).isFalse();
        
        assertThat(provider.getSelection()).isEqualTo(newSelection("viewer1_a"));
    }

    @Test
    public void selectionFromFocusedViewerIsReturned_2() {
        final TableViewer viewer1 = createViewer(shellProvider.getShell(), "viewer1_a", "viewer1_b");
        final TableViewer viewer2 = createViewer(shellProvider.getShell(), "viewer2_x", "viewer2_y");
        final ViewersCombiningSelectionProvider provider = new ViewersCombiningSelectionProvider(viewer1, viewer2);

        viewer1.setSelection(newSelection("viewer1_a"));
        viewer2.setSelection(newSelection("viewer2_y"));

        assertThat(viewer1.getTable().isFocusControl()).isFalse();
        assertThat(viewer2.getTable().isFocusControl()).isFalse();

        viewer2.getTable().forceFocus();

        assertThat(viewer1.getTable().isFocusControl()).isFalse();
        assertThat(viewer2.getTable().isFocusControl()).isTrue();

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
