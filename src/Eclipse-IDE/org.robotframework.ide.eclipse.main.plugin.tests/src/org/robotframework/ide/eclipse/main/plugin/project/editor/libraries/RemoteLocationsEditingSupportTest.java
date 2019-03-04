/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.AlwaysDeactivatingCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.viewers.ElementAddingToken;

public class RemoteLocationsEditingSupportTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void textCellEditorIsReturnedForRemoteLocation() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shellProvider.getShell());
        when(viewer.getColumnViewerEditor()).thenReturn(mock(ColumnViewerEditor.class));

        final RemoteLocationsEditingSupport support = new RemoteLocationsEditingSupport(viewer, null,
                mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        assertThat(support.getCellEditor(new RemoteLocation()))
                .isInstanceOf(ActivationCharPreservingTextCellEditor.class);
    }

    @Test
    public void alwaysDeactivatingCellEditorIsReturnedForAddingToken() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shellProvider.getShell());
        when(viewer.getColumnViewerEditor()).thenReturn(mock(ColumnViewerEditor.class));

        final RemoteLocationsEditingSupport support = new RemoteLocationsEditingSupport(viewer, null,
                mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        assertThat(support.getCellEditor(new ElementAddingToken("remote location", true)))
                .isInstanceOf(AlwaysDeactivatingCellEditor.class);
    }

    @Test
    public void uriIsReturnedAsValueToEditForRemoteLocation() {
        final RemoteLocationsEditingSupport support = new RemoteLocationsEditingSupport(mock(ColumnViewer.class), null,
                mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        final RemoteLocation location = RemoteLocation.create("http://some.uri.com");
        assertThat(support.getValue(location)).isEqualTo("http://some.uri.com");

    }

    @Test
    public void nullIsReturnedAsValueToEditForAddingToken() {
        final RemoteLocationsEditingSupport support = new RemoteLocationsEditingSupport(mock(ColumnViewer.class), null,
                mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        final ElementAddingToken addingToken = new ElementAddingToken("remote location", true);
        assertThat(support.getValue(addingToken)).isNull();
    }

    @Test
    public void whenTryingToSetInvalidUriForRemoteLocation_nothingHappens() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RemoteLocationsEditingSupport support = new RemoteLocationsEditingSupport(mock(ColumnViewer.class), null,
                mock(RedProjectEditorInput.class), eventBroker);

        final RemoteLocation location = RemoteLocation.create("http://some.uri.com");

        support.setValue(location, "invalid uri");

        assertThat(location.getUri()).isEqualTo("http://some.uri.com");
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void whenTryingToSetValidUriForRemoteLocation_uriIsChangedAndBrokerSendsTheEvent() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final IFile file = mock(IFile.class);
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getFile()).thenReturn(file);

        final RemoteLocationsEditingSupport support = new RemoteLocationsEditingSupport(mock(ColumnViewer.class), null,
                input, eventBroker);

        final RemoteLocation location = RemoteLocation.create("http://some.uri.com");

        support.setValue(location, "http://some.other.uri.com");

        assertThat(location.getUri()).isEqualTo("http://some.other.uri.com");
        verify(eventBroker).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_PATH_CHANGED),
                argThat(hasCorrectEventData(file, location)));
    }

    @Test
    public void whenTryingToSetSameUriForRemoteLocation_nothingHappens() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RemoteLocationsEditingSupport support = new RemoteLocationsEditingSupport(mock(ColumnViewer.class), null,
                mock(RedProjectEditorInput.class), eventBroker);

        final RemoteLocation location = RemoteLocation.create("http://some.uri.com");

        support.setValue(location, "http://some.uri.com");

        assertThat(location.getUri()).isEqualTo("http://some.uri.com");
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void whenTryingToSetValueForAddingToken_creatorIsCalled() {
        final AtomicBoolean creatorCalled = new AtomicBoolean(false);

        final Supplier<RemoteLocation> creator = () -> {
            creatorCalled.set(true);
            return null;
        };

        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shellProvider.getShell());
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RemoteLocationsEditingSupport support = new RemoteLocationsEditingSupport(viewer,
                creator, mock(RedProjectEditorInput.class), eventBroker);

        final ElementAddingToken addingToken = new ElementAddingToken("remote location", true);

        support.setValue(addingToken, null);

        assertThat(creatorCalled.get()).isTrue();
    }

    private static ArgumentMatcher<Object> hasCorrectEventData(final IFile file, final RemoteLocation location) {
        return object -> object instanceof RedProjectConfigEventData<?>
                && file.equals(((RedProjectConfigEventData<?>) object).getUnderlyingFile())
                && location.equals(((RedProjectConfigEventData<?>) object).getChangedElement());
    }
}
