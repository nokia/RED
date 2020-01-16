/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesEditingSupport.ReferencedLibraryCreator;
import org.robotframework.red.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.robotframework.red.jface.viewers.AlwaysDeactivatingCellEditor;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.viewers.ElementAddingToken;

@ExtendWith({ ProjectExtension.class, FreshShellExtension.class })
public class ReferencedLibrariesEditingSupportTest {

    @Project(files = { "PyLib.py", "JavaLib.jar", "JavaLib.zip" })
    static IProject project;

    @FreshShell
    Shell shell;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, "libspec.xml", "<keywordspec name=\"TestLib\" format=\"ROBOT\">",
                "<version>1.0</version><scope>global</scope>", "<doc>Documentation for test library ``TestLib``.</doc>",
                "<kw name=\"Some Keyword\"><arguments><arg>a</arg><arg>b</arg></arguments><doc></doc></kw>",
                "</keywordspec>");
    }

    @Test
    public void nullEditorIsReturnedForReferencedLibrary() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shell);
        when(viewer.getColumnViewerEditor()).thenReturn(mock(ColumnViewerEditor.class));

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(viewer, null,
                mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        final CellEditor cellEditor = support.getCellEditor(new ReferencedLibrary());
        assertThat(cellEditor).isNull();
    }

    @Test
    public void textEditorIsReturnedForRemoteLocation() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shell);
        when(viewer.getColumnViewerEditor()).thenReturn(mock(ColumnViewerEditor.class));

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(viewer, null,
                mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        final CellEditor cellEditor = support.getCellEditor(new RemoteLocation());
        assertThat(cellEditor).isInstanceOf(ActivationCharPreservingTextCellEditor.class);
    }

    @Test
    public void textEditorIsReturnedForLibraryArguments() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shell);
        when(viewer.getColumnViewerEditor()).thenReturn(mock(ColumnViewerEditor.class));

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(viewer, null,
                mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        final CellEditor cellEditor = support.getCellEditor(ReferencedLibraryArgumentsVariant.create("1"));
        assertThat(cellEditor).isInstanceOf(ActivationCharPreservingTextCellEditor.class);
    }

    @Test
    public void alwaysDeactivatingCellEditorIsReturnedForAddingToken() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shell);
        when(viewer.getColumnViewerEditor()).thenReturn(mock(ColumnViewerEditor.class));

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(viewer, null,
                mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        final CellEditor cellEditor = support.getCellEditor(new ElementAddingToken("library file", true));
        assertThat(cellEditor).isInstanceOf(AlwaysDeactivatingCellEditor.class);
    }

    @Test
    public void nullIsReturnedAsValueToEditForReferencedLibrary() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null, mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        final Object value = support.getValue(new ReferencedLibrary());
        assertThat(value).isNull();
    }

    @Test
    public void nullIsReturnedAsValueToEditForAddingToken() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null, mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        final Object value = support.getValue(new ElementAddingToken("remote location", true));
        assertThat(value).isNull();
    }

    @Test
    public void uriIsReturnedAsValueToEditForRemoteLocation() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null, mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        final Object value = support.getValue(RemoteLocation.create("http://some.uri.com"));
        assertThat(value).isEqualTo("http://some.uri.com");
    }

    @Test
    public void argumentsJoinedWithSeperatorIsReturnedAsValueToEditForLibraryArguments() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null, mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        final Object value = support.getValue(ReferencedLibraryArgumentsVariant.create("1", "2", "3"));
        assertThat(value).isEqualTo("1::2::3");
    }

    @Test
    public void whenTryingToSetValueForReferencedLibrary_nothingHappens() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null, mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        final ReferencedLibrary library = new ReferencedLibrary();

        support.setValue(library, "value");

        assertThat(library).isEqualTo(new ReferencedLibrary());
    }

    @Test
    public void whenTryingToSetInvalidUriForRemoteLocation_nothingHappens() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null, mock(RedProjectEditorInput.class), eventBroker);

        final RemoteLocation location = RemoteLocation.create("http://some.uri.com");
        support.setValue(location, "invalid uri");

        assertThat(location.getUri()).isEqualTo("http://some.uri.com");
        verifyNoInteractions(eventBroker);
    }

    @Test
    public void whenTryingToSetValidUriForRemoteLocation_uriIsChangedAndBrokerSendsTheEvent() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final IFile file = mock(IFile.class);
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getFile()).thenReturn(file);

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null, input, eventBroker);

        final RemoteLocation location = RemoteLocation.create("http://some.uri.com");

        support.setValue(location, "http://some.other.uri.com");

        assertThat(location.getUri()).isEqualTo("http://some.other.uri.com");
        verify(eventBroker).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_ARGUMENTS_CHANGED),
                argThat(hasCorrectEventData(file, newArrayList(location))));
    }

    @Test
    public void whenTryingToSetSameUriForRemoteLocation_nothingHappens() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null, mock(RedProjectEditorInput.class), eventBroker);

        final RemoteLocation location = RemoteLocation.create("http://some.uri.com");
        support.setValue(location, "http://some.uri.com");

        assertThat(location.getUri()).isEqualTo("http://some.uri.com");
        verifyNoInteractions(eventBroker);
    }

    @Test
    public void whenTryingToSetArgumentsForLibrary_argumentsAreChangedAndBrokerSendsTheEvent() {
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final IFile file = mock(IFile.class);
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getFile()).thenReturn(file);

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null, input, eventBroker);

        final ReferencedLibraryArgumentsVariant args = ReferencedLibraryArgumentsVariant.create("1", "2", "3");

        support.setValue(args, "a::b::c");

        assertThat(args.getArgsStream()).containsExactly("a", "b", "c");
        verify(eventBroker).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_ARGUMENTS_CHANGED),
                argThat(hasCorrectEventData(file, newArrayList(args))));
    }

    @Test
    public void whenTryingToSetSameArgumentsForLibrary_nothingHappens() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null, mock(RedProjectEditorInput.class), eventBroker);

        final ReferencedLibraryArgumentsVariant args = ReferencedLibraryArgumentsVariant.create("1", "2", "3");

        support.setValue(args, "1::2::3");

        assertThat(args.getArgsStream()).containsExactly("1", "2", "3");
        verifyNoInteractions(eventBroker);
    }

    @Test
    public void whenTryingToSetValueForAddingToken_creatorIsCalled() {
        final AtomicBoolean creatorCalled = new AtomicBoolean(false);

        final Supplier<ReferencedLibrary> creator = () -> {
            creatorCalled.set(true);
            return null;
        };

        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shell);

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(viewer, creator,
                mock(RedProjectEditorInput.class), mock(IEventBroker.class));

        final ElementAddingToken addingToken = new ElementAddingToken("library file", true);

        support.setValue(addingToken, null);

        assertThat(creatorCalled.get()).isTrue();
    }

    @Test
    public void virtualLibraryIsImported_whenPathPointsToLibspecFile() {
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = getFile(project, "libspec.xml").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shell, input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importLibFromSpecFile(path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsImported_whenNonJythonInterpreterIsUsedAndPathDoesNotPointToJarFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Python);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = getFile(project, "PyLib.py").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shell, input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importPythonLib(env, project, config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsImported_whenNonJythonInterpreterIsUsedAndPathPointsToJarFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Python);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = getFile(project, "JavaLib.jar").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shell, input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importPythonLib(env, project, config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsImported_whenNonJythonInterpreterIsUsedAndPathPointsToZipFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Python);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = getFile(project, "JavaLib.zip").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shell, input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importPythonLib(env, project, config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsImported_whenJythonInterpreterIsUsedAndPathDoesNotPointToJarFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Jython);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = getFile(project, "PyLib.py").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shell, input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importPythonLib(env, project, config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsImported_whenJythonInterpreterIsUsedAndPathDoesPointToJarFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Jython);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = getFile(project, "JavaLib.jar").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shell, input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importJavaLib(env, project, config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsImported_whenJythonInterpreterIsUsedAndPathDoesPointToZipFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Jython);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = getFile(project, "JavaLib.zip").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shell, input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importJavaLib(env, project, config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void javaLibraryIsImported_whenJythonInterpreterIsUsedAndPathPointsToJarFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Jython);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = getFile(project, "JavaLib.jar").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shell, input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importJavaLib(env, project, config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void javaLibraryIsImported_whenJythonInterpreterIsUsedAndPathPointsToZipFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Jython);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = getFile(project, "JavaLib.zip").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shell, input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importJavaLib(env, project, config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    private static <T> ArgumentMatcher<Object> hasCorrectEventData(final IFile file, final T data) {
        return object -> object instanceof RedProjectConfigEventData<?>
                && file.equals(((RedProjectConfigEventData<?>) object).getUnderlyingFile())
                && data.equals(((RedProjectConfigEventData<?>) object).getChangedElement());
    }
}
