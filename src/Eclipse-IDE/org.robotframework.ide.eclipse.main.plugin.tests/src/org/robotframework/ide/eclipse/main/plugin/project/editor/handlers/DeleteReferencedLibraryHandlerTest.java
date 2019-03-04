/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.DeleteReferencedLibraryHandler.E4DeleteReferencedLibraryHandler;

public class DeleteReferencedLibraryHandlerTest {

    @Test
    public void whenSomeReferencedLibrariesShouldBeRemoved_theyAreAndEventBrokerNotifiesAboutItAndWatchingOnLibrariesIsUnregistered() {
        final E4DeleteReferencedLibraryHandler handler = new E4DeleteReferencedLibraryHandler();

        final ReferencedLibrary library1 = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "path1");
        final ReferencedLibrary library2 = ReferencedLibrary.create(LibraryType.PYTHON, "OtherPyLib", "path2");
        final ReferencedLibrary library3 = ReferencedLibrary.create(LibraryType.JAVA, "JavaLib", "path3");
        final ReferencedLibrary library4 = ReferencedLibrary.create(LibraryType.VIRTUAL, "XmlLib", "path4");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(library1);
        config.addReferencedLibrary(library2);
        config.addReferencedLibrary(library3);
        config.addReferencedLibrary(library4);

        final IFile file = mock(IFile.class);
        final RobotProject project = mock(RobotProject.class);
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getFile()).thenReturn(file);
        when(input.getProjectConfiguration()).thenReturn(config);
        when(input.getRobotProject()).thenReturn(project);

        final IEventBroker eventBroker = mock(IEventBroker.class);

        final IStructuredSelection selectedLocations = new StructuredSelection(newArrayList(library2, library4));
        handler.deleteReferencedLibraries(selectedLocations, input, eventBroker);

        verify(eventBroker).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED),
                argThat(hasCorrectEventData(file, newArrayList(library2, library4))));
        verify(project).unregisterWatchingOnReferencedLibraries(newArrayList(library2, library4));
        assertThat(config.getReferencedLibraries()).containsExactly(library1, library3);
    }

    @Test
    public void whenNoReferencedLibraryWasRemoved_eventBrokerDoesNotNotifyAndWatchingOnLibrariesIsNotUnregistered() {
        final E4DeleteReferencedLibraryHandler handler = new E4DeleteReferencedLibraryHandler();

        final ReferencedLibrary library1 = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "path1");
        final ReferencedLibrary library2 = ReferencedLibrary.create(LibraryType.PYTHON, "OtherPyLib", "path2");
        final ReferencedLibrary library3 = ReferencedLibrary.create(LibraryType.JAVA, "JavaLib", "path3");
        final ReferencedLibrary library4 = ReferencedLibrary.create(LibraryType.VIRTUAL, "XmlLib", "path4");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(library1);
        config.addReferencedLibrary(library2);

        final RobotProject project = mock(RobotProject.class);
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getProjectConfiguration()).thenReturn(config);
        when(input.getRobotProject()).thenReturn(project);

        final IEventBroker eventBroker = mock(IEventBroker.class);

        final IStructuredSelection selectedLocations = new StructuredSelection(newArrayList(library3, library4));
        handler.deleteReferencedLibraries(selectedLocations, input, eventBroker);

        verifyZeroInteractions(eventBroker);
        verifyZeroInteractions(project);
        assertThat(config.getReferencedLibraries()).containsExactly(library1, library2);
    }

    private static ArgumentMatcher<Object> hasCorrectEventData(final IFile file,
            final List<ReferencedLibrary> libraries) {
        return object -> object instanceof RedProjectConfigEventData<?>
                && file.equals(((RedProjectConfigEventData<?>) object).getUnderlyingFile())
                && libraries.equals(((RedProjectConfigEventData<?>) object).getChangedElement());
    }
}
