/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.red.junit.ShellProvider;

@RunWith(MockitoJUnitRunner.class)
public class ReferencedLibrariesEditingSupportTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Mock
    private Consumer<RemoteLocation> successHandler;

    @Test
    public void editingIsSupportedOnlyForRemoteLocations() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), successHandler);

        assertThat(support.canEdit(new ReferencedLibrary())).isFalse();
        assertThat(support.canEdit(new Object())).isFalse();
        assertThat(support.canEdit(new RemoteLocation())).isTrue();
    }

    @Test
    public void properCellEditorIsProvided() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shellProvider.getShell());
        when(viewer.getColumnViewerEditor()).thenReturn(mock(ColumnViewerEditor.class));

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                viewer, successHandler);

        assertThat(support.getCellEditor(new ReferencedLibrary())).isNull();
        assertThat(support.getCellEditor(new Object())).isNull();
        assertThat(support.getCellEditor(new RemoteLocation()))
                .isInstanceOf(ActivationCharPreservingTextCellEditor.class);
    }

    @Test
    public void uriIsProvidedAsValueToEditForRemoteLocation() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), successHandler);

        final RemoteLocation location = RemoteLocation.create("http://some.uri.com");
        assertThat(support.getValue(location)).isEqualTo("http://some.uri.com");

    }

    @Test
    public void emptyStringIsProvidedAsValueToEditForOtherStuff() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), successHandler);

        assertThat(support.getValue(new ReferencedLibrary())).isEqualTo("");
        assertThat(support.getValue(new Object())).isEqualTo("");
    }

    @Test
    public void whenTryingToSetInvalidUriForRemoteLocation_nothingHappens() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), successHandler);

        final RemoteLocation location = RemoteLocation.create("http://some.uri.com");

        support.setValue(location, "invalid uri");

        assertThat(location.getUri()).isEqualTo("http://some.uri.com");
        verifyZeroInteractions(successHandler);
    }

    @Test
    public void whenTryingToSetValidUriForRemoteLocation_uriIsChangedAndBrokerSendsTheEvent() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), successHandler);

        final RemoteLocation location = RemoteLocation.create("http://some.uri.com");

        support.setValue(location, "http://some.other.uri.com");

        assertThat(location.getUri()).isEqualTo("http://some.other.uri.com");
        verify(successHandler).accept(location);
    }

    @Test
    public void whenTryingToSetSameUriForRemoteLocation_nothingHappens() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), successHandler);

        final RemoteLocation location = RemoteLocation.create("http://some.uri.com");

        support.setValue(location, "http://some.uri.com");

        assertThat(location.getUri()).isEqualTo("http://some.uri.com");
        verifyZeroInteractions(successHandler);
    }

    @Test
    public void whenTryingToSetValueForReferencedLibrary_nothingHappens() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), successHandler);

        final ReferencedLibrary library = new ReferencedLibrary();

        support.setValue(library, "somevalue");

        assertThat(library).isEqualTo(new ReferencedLibrary());
        verifyZeroInteractions(successHandler);
    }
}
