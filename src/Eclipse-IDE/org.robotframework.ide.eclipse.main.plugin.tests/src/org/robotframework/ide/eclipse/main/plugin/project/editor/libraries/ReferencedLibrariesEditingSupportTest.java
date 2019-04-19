/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesEditingSupport.ReferencedLibraryCreator;
import org.robotframework.red.jface.viewers.AlwaysDeactivatingCellEditor;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.viewers.ElementAddingToken;

public class ReferencedLibrariesEditingSupportTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ReferencedLibrariesEditingSupportTest.class);

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("PyLib.py");
        projectProvider.createFile("JavaLib.jar");
        projectProvider.createFile("JavaLib.zip");
        projectProvider.createFile("libspec.xml", "<keywordspec name=\"TestLib\" format=\"ROBOT\">",
                "<version>1.0</version><scope>global</scope>", "<doc>Documentation for test library ``TestLib``.</doc>",
                "<kw name=\"Some Keyword\"><arguments><arg>a</arg><arg>b</arg></arguments><doc></doc></kw>",
                "</keywordspec>");
    }

    @Test
    public void nullIsReturnedForReferencedLibrary() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shellProvider.getShell());
        when(viewer.getColumnViewerEditor()).thenReturn(mock(ColumnViewerEditor.class));

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(viewer, null);

        assertThat(support.getCellEditor(new ReferencedLibrary())).isNull();
    }

    @Test
    public void alwaysDeactivatingCellEditorIsReturnedForAddingToken() {
        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shellProvider.getShell());
        when(viewer.getColumnViewerEditor()).thenReturn(mock(ColumnViewerEditor.class));

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(viewer, null);

        assertThat(support.getCellEditor(new ElementAddingToken("library file", true)))
                .isInstanceOf(AlwaysDeactivatingCellEditor.class);
    }

    @Test
    public void nullIsReturnedAsValueToEditForReferencedLibrary() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null);

        final ReferencedLibrary library = new ReferencedLibrary();
        assertThat(support.getValue(library)).isNull();

    }

    @Test
    public void nullIsReturnedAsValueToEditForAddingToken() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null);

        final ElementAddingToken addingToken = new ElementAddingToken("remote location", true);
        assertThat(support.getValue(addingToken)).isNull();
    }

    @Test
    public void whenTryingToSetValueForReferencedLibrary_nothingHappens() {
        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(
                mock(ColumnViewer.class), null);

        final ReferencedLibrary library = new ReferencedLibrary();

        support.setValue(library, "value");

        assertThat(library).isEqualTo(new ReferencedLibrary());
    }

    @Test
    public void whenTryingToSetValueForAddingToken_creatorIsCalled() {
        final AtomicBoolean creatorCalled = new AtomicBoolean(false);

        final Supplier<ReferencedLibrary> creator = () -> {
            creatorCalled.set(true);
            return null;
        };

        final ColumnViewer viewer = mock(ColumnViewer.class);
        when(viewer.getControl()).thenReturn(shellProvider.getShell());

        final ReferencedLibrariesEditingSupport support = new ReferencedLibrariesEditingSupport(viewer, creator);

        final ElementAddingToken addingToken = new ElementAddingToken("library file", true);

        support.setValue(addingToken, null);

        assertThat(creatorCalled.get()).isTrue();
    }

    @Test
    public void virtualLibraryIsImported_whenPathPointsToLibspecFile() {
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = projectProvider.getFile("libspec.xml").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shellProvider.getShell(), input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importLibFromSpecFile(path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsImported_whenNonJythonInterpreterIsUsedAndPathDoesNotPointToJarFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Python);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = projectProvider.getFile("PyLib.py").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shellProvider.getShell(), input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importPythonLib(env, projectProvider.getProject(), config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsImported_whenNonJythonInterpreterIsUsedAndPathPointsToJarFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Python);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = projectProvider.getFile("JavaLib.jar").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shellProvider.getShell(), input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importPythonLib(env, projectProvider.getProject(), config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsImported_whenNonJythonInterpreterIsUsedAndPathPointsToZipFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Python);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = projectProvider.getFile("JavaLib.zip").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shellProvider.getShell(), input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importPythonLib(env, projectProvider.getProject(), config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsImported_whenJythonInterpreterIsUsedAndPathDoesNotPointToJarFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Jython);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = projectProvider.getFile("PyLib.py").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shellProvider.getShell(), input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importPythonLib(env, projectProvider.getProject(), config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsImported_whenJythonInterpreterIsUsedAndPathDoesPointToJarFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Jython);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = projectProvider.getFile("JavaLib.jar").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shellProvider.getShell(), input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importJavaLib(env, projectProvider.getProject(), config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsImported_whenJythonInterpreterIsUsedAndPathDoesPointToZipFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Jython);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = projectProvider.getFile("JavaLib.zip").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shellProvider.getShell(), input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importJavaLib(env, projectProvider.getProject(), config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void javaLibraryIsImported_whenJythonInterpreterIsUsedAndPathPointsToJarFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Jython);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = projectProvider.getFile("JavaLib.jar").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shellProvider.getShell(), input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importJavaLib(env, projectProvider.getProject(), config, path.toFile());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void javaLibraryIsImported_whenJythonInterpreterIsUsedAndPathPointsToZipFile() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final RobotProjectConfig config = new RobotProjectConfig();
        final RedProjectEditorInput input = mock(RedProjectEditorInput.class);
        when(input.getRobotProject()).thenReturn(robotProject);
        when(input.getProjectConfiguration()).thenReturn(config);
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.getInterpreter()).thenReturn(SuiteExecutor.Jython);

        final ReferencedLibraryImporter importer = mock(ReferencedLibraryImporter.class);
        final IPath path = projectProvider.getFile("JavaLib.zip").getLocation();

        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(shellProvider.getShell(), input,
                null, () -> env);

        elementsCreator.importLibrary(importer, path);

        verify(importer).importJavaLib(env, projectProvider.getProject(), config, path.toFile());
        verifyNoMoreInteractions(importer);
    }
}
