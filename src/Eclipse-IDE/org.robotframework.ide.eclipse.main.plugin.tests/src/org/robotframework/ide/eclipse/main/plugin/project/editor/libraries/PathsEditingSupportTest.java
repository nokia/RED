/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.robotframework.red.jface.viewers.AlwaysDeactivatingCellEditor;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.viewers.ElementAddingToken;

@RunWith(MockitoJUnitRunner.class)
public class PathsEditingSupportTest {

    @Rule
    public ShellProvider shell = new ShellProvider();

    @Mock
    private Consumer<SearchPath> successHandler;

    @Test
    public void editingPossibilityTest() {
        final Supplier<SearchPath> creator = () -> null;
        final ColumnViewer viewer = mock(ColumnViewer.class);

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, successHandler);

        assertThat(support.canEdit(new ElementAddingToken("search path", true))).isTrue();
        assertThat(support.canEdit(SearchPath.create("custom", false))).isTrue();
        assertThat(support.canEdit(SearchPath.create("system", true))).isFalse();
    }

    @Test
    public void textCellEditorIsReturnedForSearchPath() {
        final Supplier<SearchPath> creator = () -> null;
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shell.getShell());
        when(viewer.getColumnViewerEditor()).thenReturn(mock(ColumnViewerEditor.class));

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, successHandler);

        assertThat(support.getCellEditor(SearchPath.create("path")))
                .isInstanceOf(ActivationCharPreservingTextCellEditor.class);
    }

    @Test
    public void alwaysDeactivatingCellEditorIsReturnedForAddingToken() {
        final Supplier<SearchPath> creator = () -> null;
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shell.getShell());

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, successHandler);

        assertThat(support.getCellEditor(new ElementAddingToken("search path", true)))
                .isInstanceOf(AlwaysDeactivatingCellEditor.class);
    }

    @Test
    public void pathIsReturnedAsValueToEditForSearchPath() {
        final Supplier<SearchPath> creator = () -> null;
        final ColumnViewer viewer = mock(ColumnViewer.class);

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, successHandler);

        assertThat(support.getValue(SearchPath.create("path_to_edit"))).isEqualTo("path_to_edit");
    }

    @Test
    public void nullIsReturnedAsValueToEditForAddingToken() {
        final Supplier<SearchPath> creator = () -> null;
        final ColumnViewer viewer = mock(ColumnViewer.class);

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, successHandler);

        assertThat(support.getValue(new ElementAddingToken("search path", true))).isNull();
    }

    @Test
    public void newPathIsSetToSearchPathAndBrokerNotifiesListenersAboutIt() {
        final Supplier<SearchPath> creator = () -> null;
        final ColumnViewer viewer = mock(ColumnViewer.class);

        final SearchPath searchPath = SearchPath.create("path");

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, successHandler);
        support.setValue(searchPath, "new_path");

        assertThat(searchPath.getLocation()).isEqualTo("new_path");
        verify(successHandler).accept(searchPath);
    }

    @Test
    public void newPathIsNotSetToSearchPathAndBrokerDoesNotNotifyListenersAboutIt() {
        final Supplier<SearchPath> creator = () -> null;
        final ColumnViewer viewer = mock(ColumnViewer.class);

        final SearchPath searchPath = SearchPath.create("path");

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, successHandler);
        support.setValue(searchPath, "path");

        assertThat(searchPath.getLocation()).isEqualTo("path");
        verifyNoInteractions(successHandler);
    }

    @Test
    public void creatorIsCalledWhenSettingValueForAddingToken() {
        final AtomicBoolean creatorCalled = new AtomicBoolean(false);

        final Supplier<SearchPath> creator = () -> {
            creatorCalled.set(true);
            return null;
        };
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shell.getShell());

        final ElementAddingToken addingToken = new ElementAddingToken("search path", true);

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, successHandler);
        support.setValue(addingToken, null);

        assertThat(creatorCalled.get()).isTrue();
    }
}
