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
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.DeleteReferencedLibraryHandler.E4DeleteReferencedLibraryHandler;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlArgumentsVariant;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlArgumentsVariant.RedXmlRemoteArgumentsVariant;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlLibrary.RedXmlRemoteLib;

public class DeleteReferencedLibraryHandlerTest {

    @Test
    public void whenSomeReferencedLibrariesShouldBeRemoved_theyAreAndEventBrokerNotifiesAboutItAndWatchingOnLibrariesIsUnregistered() {
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
        final RedProjectEditorInput input = prepareInput(config, file, project);

        final IEventBroker eventBroker = mock(IEventBroker.class);

        final List<RedXmlLibrary> selection = newArrayList(new RedXmlLibrary(library2), new RedXmlLibrary(library4));
        final IStructuredSelection selectedLibs = new StructuredSelection(selection);

        final E4DeleteReferencedLibraryHandler handler = new E4DeleteReferencedLibraryHandler();
        handler.deleteReferencedLibraries(selectedLibs, input, eventBroker);

        verify(eventBroker).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED), argThat(
                hasCorrectEventData(file, newArrayList(new RedXmlLibrary(library2), new RedXmlLibrary(library4)))));
        verify(project).unregisterWatchingOnReferencedLibraries(newArrayList(library2, library4));
        assertThat(config.getReferencedLibraries()).containsExactly(library1, library3);
    }

    @Test
    public void whenRemoteLibIsSelected_nothingHappens() {
        final RobotProjectConfig config = new RobotProjectConfig();

        final IFile file = mock(IFile.class);
        final RobotProject project = mock(RobotProject.class);
        final RedProjectEditorInput input = prepareInput(config, file, project);

        final IEventBroker eventBroker = mock(IEventBroker.class);

        final IStructuredSelection selectedLibs = new StructuredSelection(newArrayList(new RedXmlRemoteLib()));

        final E4DeleteReferencedLibraryHandler handler = new E4DeleteReferencedLibraryHandler();
        handler.deleteReferencedLibraries(selectedLibs, input, eventBroker);

        verifyZeroInteractions(eventBroker);
        verifyZeroInteractions(project);
    }

    @Test
    public void whenNoReferencedLibraryWasRemoved_eventBrokerDoesNotNotifyAndWatchingOnLibrariesIsNotUnregistered() {
        final ReferencedLibrary library1 = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "path1");
        final ReferencedLibrary library2 = ReferencedLibrary.create(LibraryType.PYTHON, "OtherPyLib", "path2");
        final ReferencedLibrary library3 = ReferencedLibrary.create(LibraryType.JAVA, "JavaLib", "path3");
        final ReferencedLibrary library4 = ReferencedLibrary.create(LibraryType.VIRTUAL, "XmlLib", "path4");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(library1);
        config.addReferencedLibrary(library2);

        final RobotProject project = mock(RobotProject.class);
        final RedProjectEditorInput input = prepareInput(config, null, project);

        final IEventBroker eventBroker = mock(IEventBroker.class);

        final List<RedXmlLibrary> selection = newArrayList(new RedXmlLibrary(library3), new RedXmlLibrary(library4));
        final IStructuredSelection selectedLocations = new StructuredSelection(selection);

        final E4DeleteReferencedLibraryHandler handler = new E4DeleteReferencedLibraryHandler();
        handler.deleteReferencedLibraries(selectedLocations, input, eventBroker);

        verifyZeroInteractions(eventBroker);
        verifyZeroInteractions(project);
        assertThat(config.getReferencedLibraries()).containsExactly(library1, library2);
    }

    @Test
    public void whenArgumentsAreSelected_theyAreProperlyRemovedFromConfig() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "path1");
        library.addArgumentsVariant(ReferencedLibraryArgumentsVariant.create("1", "2"));
        final ReferencedLibraryArgumentsVariant variant2 = ReferencedLibraryArgumentsVariant.create("x", "y");
        library.addArgumentsVariant(variant2);

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(library);
        final RemoteLocation remote1 = RemoteLocation.create("http://127.0.0.1:8270/");
        config.addRemoteLocation(remote1);
        config.addRemoteLocation(RemoteLocation.create("http://127.0.0.2:8270/"));

        final IFile file = mock(IFile.class);
        final RobotProject project = mock(RobotProject.class);
        final RedProjectEditorInput input = prepareInput(config, file, project);

        final IEventBroker eventBroker = mock(IEventBroker.class);

        final List<RedXmlArgumentsVariant> selection = newArrayList(
                new RedXmlRemoteArgumentsVariant(new RedXmlRemoteLib(), remote1),
                new RedXmlArgumentsVariant(new RedXmlLibrary(library), variant2));
        final IStructuredSelection selectedLocations = new StructuredSelection(selection);

        final E4DeleteReferencedLibraryHandler handler = new E4DeleteReferencedLibraryHandler();
        handler.deleteReferencedLibraries(selectedLocations, input, eventBroker);

        verifyZeroInteractions(project);
        verify(eventBroker).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED), argThat(
                hasCorrectEventData(file, newArrayList(new RedXmlRemoteLib(), new RedXmlLibrary(library)))));
        assertThat(config.getReferencedLibraries()).containsExactly(library);
        assertThat(config.getReferencedLibraries().get(0).getArgumentsVariants())
                .containsExactly(ReferencedLibraryArgumentsVariant.create("1", "2"));
        assertThat(config.getRemoteLocations()).containsExactly(RemoteLocation.create("http://127.0.0.2:8270/"));
    }

    private static RedProjectEditorInput prepareInput(final RobotProjectConfig config, final IFile file,
            final RobotProject project) {
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getFile()).thenReturn(file);
        when(input.getProjectConfiguration()).thenReturn(config);
        when(input.getRobotProject()).thenReturn(project);
        return input;
    }

    private static <T> ArgumentMatcher<Object> hasCorrectEventData(final IFile file, final List<T> libraries) {
        return object -> object instanceof RedProjectConfigEventData<?>
                && file.equals(((RedProjectConfigEventData<?>) object).getUnderlyingFile())
                && libraries.equals(((RedProjectConfigEventData<?>) object).getChangedElement());
    }
}
