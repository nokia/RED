/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.AlwaysDeactivatingCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.ElementsAddingEditingSupport.NewElementsCreator;

public class PathsEditingSupportTest {

    @Rule
    public ShellProvider shell = new ShellProvider();

    @Test
    public void editingPossibilityTest() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final NewElementsCreator<SearchPath> creator = new NewElementsCreator<SearchPath>() {};
        final ColumnViewer viewer = mock(ColumnViewer.class);

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, eventBroker, "topic");
        
        assertThat(support.canEdit(new ElementAddingToken("search path", true))).isTrue();
        assertThat(support.canEdit(SearchPath.create("custom", false))).isTrue();
        assertThat(support.canEdit(SearchPath.create("system", true))).isFalse();
    }

    @Test
    public void textCellEditorIsReturnedForSearchPath() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final NewElementsCreator<SearchPath> creator = new NewElementsCreator<SearchPath>() {};
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shell.getShell());

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, eventBroker, "topic");

        assertThat(support.getCellEditor(SearchPath.create("path"))).isInstanceOf(TextCellEditor.class);
    }

    @Test
    public void alwaysDeactivatingCellEditorIsReturnedForAddingToken() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final NewElementsCreator<SearchPath> creator = new NewElementsCreator<SearchPath>() {};
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shell.getShell());

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, eventBroker, "topic");

        assertThat(support.getCellEditor(new ElementAddingToken("search path", true)))
                .isInstanceOf(AlwaysDeactivatingCellEditor.class);
    }

    @Test
    public void pathIsReturnedAsValueToEditForSearchPath() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final NewElementsCreator<SearchPath> creator = new NewElementsCreator<SearchPath>() {};
        final ColumnViewer viewer = mock(ColumnViewer.class);

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, eventBroker, "topic");

        assertThat(support.getValue(SearchPath.create("path_to_edit"))).isEqualTo("path_to_edit");
    }

    @Test
    public void nullIsReturnedAsValueToEditForAddingToken() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final NewElementsCreator<SearchPath> creator = new NewElementsCreator<SearchPath>() {};
        final ColumnViewer viewer = mock(ColumnViewer.class);

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, eventBroker, "topic");

        assertThat(support.getValue(new ElementAddingToken("search path", true))).isNull();
    }

    @Test
    public void newPathIsSetToSearchPathAndBrokerNotifiesListenersAboutIt() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final NewElementsCreator<SearchPath> creator = new NewElementsCreator<SearchPath>() {        };
        final ColumnViewer viewer = mock(ColumnViewer.class);

        final SearchPath searchPath = SearchPath.create("path");

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, eventBroker, "topic");
        support.setValue(searchPath, "new_path");

        assertThat(searchPath.getLocation()).isEqualTo("new_path");
        verify(eventBroker, times(1)).send("topic", searchPath);
    }
    
    @Test
    public void creatorIsCalledWhenSettingValueForAddingToken() {
        final AtomicBoolean creatorCalled = new AtomicBoolean(false);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final NewElementsCreator<SearchPath> creator = new NewElementsCreator<SearchPath>() {
            @Override
            public SearchPath createNew() {
                creatorCalled.set(true);
                return null;
            }
        };
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shell.getShell());

        final ElementAddingToken addingToken = new ElementAddingToken("search path", true);

        final PathsEditingSupport support = new PathsEditingSupport(viewer, creator, eventBroker, "topic");
        support.setValue(addingToken, null);

        assertThat(creatorCalled.get()).isTrue();
    }
}
