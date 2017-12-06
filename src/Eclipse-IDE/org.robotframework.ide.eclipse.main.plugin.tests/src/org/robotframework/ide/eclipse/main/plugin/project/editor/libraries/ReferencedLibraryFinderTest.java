/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.assertj.core.api.Condition;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.project.ResolvedImportPath.MalformedPathImportException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryFinder.IncorrectLibraryPathException;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryFinder.UnknownLibraryException;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Objects;

@RunWith(MockitoJUnitRunner.class)
public class ReferencedLibraryFinderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ReferencedLibraryFinderTest.class);

    private static RobotModel model = new RobotModel();

    @Mock
    private ReferencedLibraryImporter importer;

    private RobotProject robotProject;

    private RobotSuiteFile suite;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("suite.robot");
        projectProvider.createFile("dir_lib.py");
        projectProvider.createDir("python_path");
        projectProvider.createFile("python_path/path_lib.py");
        projectProvider.createDir("python_path/py_module");
        projectProvider.createFile("python_path/py_module/__init__.py");
        projectProvider.createFile("JavaLib.jar");

        final RobotProjectConfig projectConfig = RobotProjectConfig.create();
        final String pythonPath = projectProvider.getDir("python_path").getLocation().toFile().getAbsolutePath();
        projectConfig.addPythonPath(SearchPath.create(pythonPath));
        projectProvider.configure(projectConfig);
    }

    @Before
    public void beforeTest() throws Exception {
        robotProject = model.createRobotProject(projectProvider.getProject());
        suite = model.createSuiteFile(projectProvider.getFile("suite.robot"));
    }

    @AfterClass
    public static void afterSuite() {
        model = null;
    }

    @Test
    public void exceptionIsThrown_whenPathHasIncorrectExtension() throws Exception {
        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suite, importer);

        assertThatExceptionOfType(IncorrectLibraryPathException.class)
                .isThrownBy(() -> finder.findByPath(robotProject.getRobotProjectConfig(), "lib.js"))
                .withNoCause()
                .withMessage("The path 'lib.js' should point to either .py file or python module directory.");

        verifyZeroInteractions(importer);
    }

    @Test
    public void exceptionIsThrown_whenPathPointsToNotExistingFile() throws Exception {
        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suite, importer);

        assertThatExceptionOfType(IncorrectLibraryPathException.class)
                .isThrownBy(() -> finder.findByPath(robotProject.getRobotProjectConfig(), "not_existing.py"))
                .withNoCause()
                .withMessage("Unable to find library under 'not_existing.py' location.");

        verifyZeroInteractions(importer);
    }

    @Test
    public void exceptionIsThrown_whenPathIsMalformed() throws Exception {
        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suite, importer);

        assertThatExceptionOfType(IncorrectLibraryPathException.class)
                .isThrownBy(() -> finder.findByPath(robotProject.getRobotProjectConfig(), "{}/path_lib.py"))
                .withCauseInstanceOf(MalformedPathImportException.class);

        verifyZeroInteractions(importer);
    }

    @Test
    public void exceptionIsThrown_whenPathHasUnknownVariable() throws Exception {
        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suite, importer);

        assertThatExceptionOfType(IncorrectLibraryPathException.class)
                .isThrownBy(() -> finder.findByPath(robotProject.getRobotProjectConfig(), "${unknown}/path_lib.py"))
                .withNoCause()
                .withMessage("Unable to find library under '${unknown}/path_lib.py' location.");

        verifyZeroInteractions(importer);
    }

    @Test
    public void pythonLibraryIsFoundByPath() throws Exception {
        final IResource libResource = projectProvider.getFile("dir_lib.py");
        setupPythonImport(libResource);

        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suite, importer);
        final Collection<ReferencedLibrary> libs = finder.findByPath(robotProject.getRobotProjectConfig(),
                "dir_lib.py");

        assertThat(libs).hasSize(1);
        assertThat(libs.iterator().next()).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "dir_lib", libResource.getLocation().toPortableString())));

        verify(importer).importPythonLib(suite.getProject().getRuntimeEnvironment(), projectProvider.getProject(),
                robotProject.getRobotProjectConfig(), libResource.getLocation().toFile().getAbsolutePath());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryIsFoundByPathWithKnownVariable() throws Exception {
        final IResource libResource = projectProvider.getFile("dir_lib.py");
        setupPythonImport(libResource);

        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suite, importer);
        final Collection<ReferencedLibrary> libs = finder.findByPath(robotProject.getRobotProjectConfig(),
                "${CURDIR}/dir_lib.py");

        assertThat(libs).hasSize(1);
        assertThat(libs.iterator().next()).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "dir_lib", libResource.getLocation().toPortableString())));

        verify(importer).importPythonLib(suite.getProject().getRuntimeEnvironment(), projectProvider.getProject(),
                robotProject.getRobotProjectConfig(), libResource.getLocation().toFile().getAbsolutePath());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonModuleLibraryIsFoundByPath() throws Exception {
        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suite, importer);
        final Collection<ReferencedLibrary> libs = finder.findByPath(robotProject.getRobotProjectConfig(),
                "python_path/py_module/");

        assertThat(libs).hasSize(1);
        assertThat(libs.iterator().next()).has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "py_module",
                projectProvider.getDir("python_path/py_module").getLocation().toPortableString())));

        verifyZeroInteractions(importer);
    }

    @Test
    public void exceptionIsThrown_whenLibraryDoesNotExist() throws Exception {
        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suite, importer);

        assertThatExceptionOfType(UnknownLibraryException.class)
                .isThrownBy(() -> finder.findByName(robotProject.getRobotProjectConfig(), "not_existing"))
                .withCauseInstanceOf(RobotEnvironmentException.class);

        verifyZeroInteractions(importer);
    }

    @Test
    public void exceptionIsThrown_whenRobotEnvironmentIsInvalid() throws Exception {
        final RobotProject robotProjectSpy = spy(robotProject);
        when(robotProjectSpy.getRuntimeEnvironment()).thenReturn(mock(RobotRuntimeEnvironment.class));
        final RobotSuiteFile suiteSpy = spy(suite);
        when(suiteSpy.getProject()).thenReturn(robotProjectSpy);

        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suiteSpy, importer);

        assertThatExceptionOfType(UnknownLibraryException.class)
                .isThrownBy(() -> finder.findByName(robotProject.getRobotProjectConfig(), "unknown"))
                .withNoCause()
                .withMessage("Unable to find 'unknown' library.");

        verifyZeroInteractions(importer);
    }

    @Test
    public void pythonModuleLibraryIsFoundByName() throws Exception {
        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suite, importer);
        final Collection<ReferencedLibrary> libs = finder.findByName(robotProject.getRobotProjectConfig(), "py_module");

        assertThat(libs).hasSize(1);
        assertThat(libs.iterator().next()).has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "py_module",
                projectProvider.getDir("python_path/py_module").getLocation().toPortableString())));

        verifyZeroInteractions(importer);
    }

    @Test
    public void pythonLibraryFromAdditionalPythonPathIsFoundByName() throws Exception {
        final IResource libResource = projectProvider.getFile("python_path/path_lib.py");
        setupPythonImport(libResource);

        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suite, importer);
        final Collection<ReferencedLibrary> libs = finder.findByName(robotProject.getRobotProjectConfig(), "path_lib");

        assertThat(libs).hasSize(1);
        assertThat(libs.iterator().next()).has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "path_lib",
                libResource.getLocation().toPortableString())));

        verify(importer).importPythonLib(suite.getProject().getRuntimeEnvironment(), projectProvider.getProject(),
                robotProject.getRobotProjectConfig(), libResource.getLocation().toFile().getAbsolutePath());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void pythonLibraryFromCurrentDirIsFoundByName() throws Exception {
        final IResource libResource = projectProvider.getFile("dir_lib.py");
        setupPythonImport(libResource);

        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suite, importer);
        final Collection<ReferencedLibrary> libs = finder.findByName(robotProject.getRobotProjectConfig(), "dir_lib");

        assertThat(libs).hasSize(1);
        assertThat(libs.iterator().next()).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "dir_lib", libResource.getLocation().toPortableString())));

        verify(importer).importPythonLib(suite.getProject().getRuntimeEnvironment(), projectProvider.getProject(),
                robotProject.getRobotProjectConfig(), libResource.getLocation().toFile().getAbsolutePath());
        verifyNoMoreInteractions(importer);
    }

    @Test
    public void javaLibraryIsFoundByName() throws Exception {
        final IResource libResource = projectProvider.getFile("JavaLib.jar");
        setupJavaImport(libResource);

        final RobotRuntimeEnvironment environment = mock(RobotRuntimeEnvironment.class);
        when(environment.getModulePath(eq("JavaLib"), any(EnvironmentSearchPaths.class)))
                .thenReturn(Optional.of(libResource.getLocation().toFile()));
        final RobotProject robotProjectSpy = spy(robotProject);
        when(robotProjectSpy.getRuntimeEnvironment()).thenReturn(environment);
        final RobotSuiteFile suiteSpy = spy(suite);
        when(suiteSpy.getProject()).thenReturn(robotProjectSpy);

        final ReferencedLibraryFinder finder = new ReferencedLibraryFinder(suiteSpy, importer);
        final Collection<ReferencedLibrary> libs = finder.findByName(robotProject.getRobotProjectConfig(), "JavaLib");

        assertThat(libs).hasSize(1);
        assertThat(libs.iterator().next()).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.JAVA, "JavaLib", libResource.getLocation().toPortableString())));

        verify(importer).importJavaLib(environment, projectProvider.getProject(), robotProject.getRobotProjectConfig(),
                libResource.getLocation().toString());
        verifyNoMoreInteractions(importer);
    }

    private void setupPythonImport(final IResource libResource) {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON,
                libResource.getLocation().removeFileExtension().lastSegment(),
                libResource.getLocation().toPortableString());
        when(importer.importPythonLib(any(RobotRuntimeEnvironment.class), any(IProject.class),
                any(RobotProjectConfig.class), anyString())).thenReturn(Collections.singletonList(lib));
    }

    private void setupJavaImport(final IResource libResource) {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.JAVA,
                libResource.getLocation().removeFileExtension().lastSegment(),
                libResource.getLocation().toPortableString());
        when(importer.importJavaLib(any(RobotRuntimeEnvironment.class), any(IProject.class),
                any(RobotProjectConfig.class), anyString())).thenReturn(Collections.singletonList(lib));
    }

    private static Condition<? super ReferencedLibrary> sameFieldsAs(final ReferencedLibrary library) {
        return new Condition<ReferencedLibrary>() {

            @Override
            public boolean matches(final ReferencedLibrary toMatch) {
                return Objects.equal(library.getType(), toMatch.getType())
                        && Objects.equal(library.getName(), toMatch.getName())
                        && Objects.equal(library.getPath(), toMatch.getPath());
            }
        };
    }

}
