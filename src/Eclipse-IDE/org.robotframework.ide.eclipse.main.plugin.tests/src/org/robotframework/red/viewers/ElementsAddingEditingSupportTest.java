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
import static org.mockito.Mockito.verifyZeroInteractions;

import org.eclipse.jface.viewers.AlwaysDeactivatingCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.viewers.ElementsAddingEditingSupport.NewElementsCreator;

public class ElementsAddingEditingSupportTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void columnShiftIsZero() {
        final Shell shell = shellProvider.getShell();
        final ElementsAddingEditingSupport support = createSupport(createViewer(shell, "abc", "def"), null);

        assertThat(support.getColumnShift()).isEqualTo(0);
    }

    @Test
    public void itIsAlwaysPossibleToEdit() {
        final Shell shell = shellProvider.getShell();
        final ElementsAddingEditingSupport support = createSupport(createViewer(shell, "abc", "def"), null);

        assertThat(support.canEdit(null)).isTrue();
        assertThat(support.canEdit(new Object())).isTrue();
        assertThat(support.canEdit("123")).isTrue();
        assertThat(support.canEdit("abc")).isTrue();
    }

    @Test
    public void thereAreNoCellEditorsForArbitraryObjects() {
        final Shell shell = shellProvider.getShell();
        final ElementsAddingEditingSupport support = createSupport(createViewer(shell, "abc", "def"), null);

        assertThat(support.getCellEditor(null)).isNull();
        assertThat(support.getCellEditor(new Object())).isNull();
        assertThat(support.getCellEditor("123")).isNull();
        assertThat(support.getCellEditor("abc")).isNull();
    }

    @Test
    public void whenElementAddingTokenIsGiven_alwaysDeactivatingCellEditorIsReturned() {
        final Shell shell = shellProvider.getShell();
        final ElementsAddingEditingSupport support = createSupport(createViewer(shell, "abc", "def"), null);

        assertThat(support.getCellEditor(new ElementAddingToken("element", true)))
                .isInstanceOf(AlwaysDeactivatingCellEditor.class);
    }

    @Test
    public void nothingHappensWhenTryingToEditArbitraryObject() {
        final TableViewer viewer = mock(TableViewer.class);

        final NewElementsCreator<?> creator = mock(NewElementsCreator.class);
        final ElementsAddingEditingSupport support = createSupport(viewer, creator);

        support.setValue("123", "val");
        support.setValue("abc", "val");

        verifyZeroInteractions(viewer, creator);
    }

    @Test
    public void whenEditingElementAddingToken_newElementIsCreated() {
        final Shell shell = shellProvider.getShell();

        final NewElementsCreator<?> creator = mock(NewElementsCreator.class);
        final ElementsAddingEditingSupport support = createSupport(createViewer(shell, "abc", "def"), creator);

        support.setValue(new ElementAddingToken("element", true), null);

        verify(creator, times(1)).createNew(null);

        Display.getCurrent().readAndDispatch(); // drain all events waiting for GUI thread
    }

    @Test
    public void refreshingOperationWillRefreshTheViewerAndOpenProperCellEditor() {
        final TableViewer viewer = mock(TableViewer.class);

        final ElementsAddingEditingSupport support = createSupport(viewer, null);

        support.refreshAndEdit("abc").run();

        verify(viewer, times(1)).refresh();
        verify(viewer, times(1)).editElement("abc", 0);
    }

    @Test
    public void baseCreatorCreatesJustNulls() {
        final NewElementsCreator<String> creator = new NewElementsCreator<String>() {
        };

        assertThat(creator.createNew()).isNull();
        assertThat(creator.createNew(null)).isNull();
        assertThat(creator.createNew("1")).isNull();
        assertThat(creator.createNew("abc")).isNull();
    }

    @Test
    public void baseCreatorCallsBaseCreateMethodWhenParentIsGiven() {
        final NewElementsCreator<String> creator = new NewElementsCreator<String>() {
            @Override
            public String createNew() {
                return "abc";
            }
        };

        assertThat(creator.createNew()).isEqualTo("abc");
        assertThat(creator.createNew(null)).isEqualTo("abc");
        assertThat(creator.createNew("1")).isEqualTo("abc");
        assertThat(creator.createNew("abc")).isEqualTo("abc");
    }

    private static ElementsAddingEditingSupport createSupport(final ColumnViewer viewer,
            final NewElementsCreator<?> creator) {
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
