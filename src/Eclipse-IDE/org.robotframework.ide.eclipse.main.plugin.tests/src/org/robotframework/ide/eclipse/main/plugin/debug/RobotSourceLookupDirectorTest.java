/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.model.IStackFrame;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.SourceInLibraryEditorInput.SourceOfStackFrameInLibrary;
import org.robotframework.ide.eclipse.main.plugin.debug.SourceNotFoundEditorInput.SourceOfStackFrameNotFound;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotStackFrame;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.red.junit.ProjectProvider;

public class RobotSourceLookupDirectorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotSourceLookupDirectorTest.class);

    @SuppressWarnings("unused")
    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeTest() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();
    }

    @AfterClass
    public static void afterTest() {
        RedPlugin.getModelManager().dispose();
        robotModel = null;
    }

    @Test
    public void nothingHappens_whenParticipantsShouldBeInitialized() {
        final RobotSourceLookupDirector sourceDirector = spy(new RobotSourceLookupDirector());
        sourceDirector.initializeParticipants();

        verify(sourceDirector).initializeParticipants();
        verifyNoMoreInteractions(sourceDirector);
    }

    @Test
    public void fileIsReturned_whenSourceOfFrameIsFound() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot");
        
        final RobotStackFrame frame = mock(RobotStackFrame.class);
        when(frame.getPath()).thenReturn(Optional.of(file.getLocationURI()));

        final RobotSourceLookupDirector sourceDirector = new RobotSourceLookupDirector();
        final List<Object> sources = sourceDirector.doSourceLookup(frame);

        assertThat(sources).containsExactly(file);
    }

    @Test
    public void sourceOfStackFrameNotFoundIsReturned_whenSourceOfFrameIsNotFoundAndFrameIsErroneous() throws Exception {
        final RobotStackFrame frame = mock(RobotStackFrame.class);
        when(frame.getPath()).thenReturn(Optional.of(URI.create("file:///some/path/to/file.robot")));
        when(frame.isErroneous()).thenReturn(true);

        final RobotSourceLookupDirector sourceDirector = new RobotSourceLookupDirector();
        final List<Object> sources = sourceDirector.doSourceLookup(frame);

        assertThat(sources).hasSize(1);
        assertThat(sources.get(0)).isInstanceOf(SourceOfStackFrameNotFound.class);
    }

    @Test
    public void sourceOfStackFrameInLibraryIsReturned_whenSourceOfFrameIsNotFoundAndFrameIsErroneous()
            throws Exception {
        final RobotStackFrame frame = mock(RobotStackFrame.class);
        when(frame.getPath()).thenReturn(Optional.of(URI.create("file:///some/path/to/file.robot")));
        when(frame.isLibraryKeywordFrame()).thenReturn(true);

        final RobotSourceLookupDirector sourceDirector = new RobotSourceLookupDirector();
        final List<Object> sources = sourceDirector.doSourceLookup(frame);

        assertThat(sources).hasSize(1);
        assertThat(sources.get(0)).isInstanceOf(SourceOfStackFrameInLibrary.class);
    }

    @Test
    public void nonEmptyListIsReturned_whenSourceOfFrameIsNotFoundButItIsNotErroneousNorLibraryFound()
            throws Exception {
        final RobotStackFrame frame = mock(RobotStackFrame.class);
        when(frame.getPath()).thenReturn(Optional.empty());

        final RobotSourceLookupDirector sourceDirector = new RobotSourceLookupDirector();
        final List<Object> sources = sourceDirector.doSourceLookup(frame);

        assertThat(sources).containsExactly("<avoiding null source>");
    }

    @Test
    public void noSourceIsFound_whenLookingForOrdinaryFrameSource() {
        final IStackFrame frame = mock(IStackFrame.class);

        final RobotSourceLookupDirector sourceDirector = new RobotSourceLookupDirector();
        final List<Object> sources = sourceDirector.doSourceLookup(frame);

        assertThat(sources).isEmpty();
    }
}
