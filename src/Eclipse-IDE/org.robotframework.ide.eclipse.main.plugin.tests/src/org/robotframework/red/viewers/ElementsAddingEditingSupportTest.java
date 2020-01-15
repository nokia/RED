/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.function.Supplier;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.jface.viewers.AlwaysDeactivatingCellEditor;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;

@ExtendWith(FreshShellExtension.class)
public class ElementsAddingEditingSupportTest {

    @FreshShell
    Shell shell;

    @Test
    public void columnShiftIsZero() {
        final ElementsAddingEditingSupport support = createSupport(createViewer(shell, "abc", "def"), null);

        assertThat(support.getColumnShift()).isEqualTo(0);
    }

    @Test
    public void itIsAlwaysPossibleToEdit() {
        final ElementsAddingEditingSupport support = createSupport(createViewer(shell, "abc", "def"), null);

        assertThat(support.canEdit(null)).isTrue();
        assertThat(support.canEdit(new Object())).isTrue();
        assertThat(support.canEdit("123")).isTrue();
        assertThat(support.canEdit("abc")).isTrue();
    }

    @Test
    public void thereAreNoCellEditorsForArbitraryObjects() {
        final ElementsAddingEditingSupport support = createSupport(createViewer(shell, "abc", "def"), null);

        assertThat(support.getCellEditor(null)).isNull();
        assertThat(support.getCellEditor(new Object())).isNull();
        assertThat(support.getCellEditor("123")).isNull();
        assertThat(support.getCellEditor("abc")).isNull();
    }

    @Test
    public void whenElementAddingTokenIsGiven_alwaysDeactivatingCellEditorIsReturned() {
        final ElementsAddingEditingSupport support = createSupport(createViewer(shell, "abc", "def"), null);

        assertThat(support.getCellEditor(new ElementAddingToken("element", true)))
                .isInstanceOf(AlwaysDeactivatingCellEditor.class);
    }

    @Test
    public void nothingHappensWhenTryingToEditArbitraryObject() {
        final TableViewer viewer = mock(TableViewer.class);

        final Supplier<?> creator = mock(Supplier.class);
        final ElementsAddingEditingSupport support = createSupport(viewer, creator);

        support.setValue("123", "val");
        support.setValue("abc", "val");

        verifyNoInteractions(viewer, creator);
    }

    @Test
    public void whenEditingElementAddingToken_newElementIsCreated() {
        final Supplier<?> creator = mock(Supplier.class);
        final ElementsAddingEditingSupport support = createSupport(createViewer(shell, "abc", "def"), creator);

        support.setValue(new ElementAddingToken("element", true), null);

        verify(creator, times(1)).get();

        while (Display.getDefault().readAndDispatch()) {
            // handle all events coming to UI
        }
    }

    @Test
    public void refreshingOperationWillRefreshTheViewerAndOpenProperCellEditor() {
        final TableViewer viewer = mock(TableViewer.class);

        final ElementsAddingEditingSupport support = createSupport(viewer, null);

        support.refreshAndEdit("abc").run();

        verify(viewer, times(1)).refresh();
        verify(viewer, times(1)).editElement("abc", 0);
    }

    private static ElementsAddingEditingSupport createSupport(final ColumnViewer viewer,
            final Supplier<?> creator) {
        return new ElementsAddingEditingSupport(viewer, 0, creator) {
            @Override
            protected Object getValue(final Object element) {
                return null;
            }
        };
    }

    private static TableViewer createViewer(final Composite parent, final String... inputElements) {
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
