/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryLocator.IReferencedLibraryDetector;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Objects;

@RunWith(MockitoJUnitRunner.class)
public class ReferencedLibraryLocatorTest {

    private static final String PROJECT_NAME = ReferencedLibraryLocatorTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    private static RobotModel model = new RobotModel();

    @Mock
    private IReferencedLibraryImporter importer;

    @Mock
    private IReferencedLibraryDetector detector;

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
    public void detectingFails_whenPathHasIncorrectExtension() throws Exception {
        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByPath(suite, "lib.js");

        verifyZeroInteractions(importer);

        verify(detector).libraryDetectingByPathFailed("lib.js", Optional.empty(),
                "The path 'lib.js' should point to either .py file or python module directory.");
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void detectingFails_whenPathPointsToNotExistingFile() throws Exception {
        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByPath(suite, "not_existing.py");

        verifyZeroInteractions(importer);

        verify(detector).libraryDetectingByPathFailed("not_existing.py", Optional.empty(),
                "Unable to find library under 'not_existing.py' location.");
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void detectingFails_whenPathIsMalformed() throws Exception {
        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByPath(suite, "{}/path_lib.py");

        verifyZeroInteractions(importer);

        verify(detector).libraryDetectingByPathFailed(eq("{}/path_lib.py"), eq(Optional.empty()),
                startsWith("java.net.URISyntaxException"));
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void detectingFails_whenPathHasUnknownVariable() throws Exception {
        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByPath(suite, "${unknown}/path_lib.py");

        verifyZeroInteractions(importer);

        verify(detector).libraryDetectingByPathFailed("${unknown}/path_lib.py", Optional.empty(),
                "Unable to find library under '${unknown}/path_lib.py' location.");
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void detectingFails_whenImportingByPathThrowsException() throws Exception {
        final IResource libResource = projectProvider.getFile("dir_lib.py");
        setupFailedPythonImport();

        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByPath(suite, "dir_lib.py");

        verify(importer).importPythonLib(robotProject.getRuntimeEnvironment(), projectProvider.getProject(),
                robotProject.getRobotProjectConfig(), libResource.getLocation().toFile().getAbsolutePath());
        verifyNoMoreInteractions(importer);

        verify(detector).libraryDetectingByPathFailed("dir_lib.py", Optional.of(libResource.getLocation().toFile()),
                "fail reason");
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void pythonLibraryIsFoundByPath() throws Exception {
        final IResource libResource = projectProvider.getFile("dir_lib.py");
        setupPythonImport(libResource);

        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByPath(suite, "dir_lib.py");

        verify(importer).importPythonLib(robotProject.getRuntimeEnvironment(), projectProvider.getProject(),
                robotProject.getRobotProjectConfig(), libResource.getLocation().toFile().getAbsolutePath());
        verifyNoMoreInteractions(importer);

        verify(detector).libraryDetectedByPath(eq("dir_lib.py"),
                eq(projectProvider.getFile("dir_lib.py").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.PYTHON, "dir_lib", libResource)));
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void pythonLibraryIsFoundByPathWithKnownVariable() throws Exception {
        final IResource libResource = projectProvider.getFile("dir_lib.py");
        setupPythonImport(libResource);

        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByPath(suite, "${CURDIR}/dir_lib.py");

        verify(importer).importPythonLib(robotProject.getRuntimeEnvironment(), projectProvider.getProject(),
                robotProject.getRobotProjectConfig(), libResource.getLocation().toFile().getAbsolutePath());
        verifyNoMoreInteractions(importer);

        verify(detector).libraryDetectedByPath(eq("${CURDIR}/dir_lib.py"),
                eq(projectProvider.getFile("dir_lib.py").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.PYTHON, "dir_lib", libResource)));
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void pythonModuleLibraryIsFound_whenPathIsAbsolute() throws Exception {
        final String absolutePath = projectProvider.getDir("python_path/py_module").getLocation().toPortableString();
        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByPath(suite, absolutePath);

        verifyZeroInteractions(importer);

        verify(detector).libraryDetectedByPath(eq(absolutePath),
                eq(projectProvider.getFile("python_path/py_module/__init__.py").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.PYTHON, "py_module", projectProvider.getDir("python_path"))));
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void pythonModuleLibraryIsFound_whenPathIsAbsoluteWithTrailingSeparator() throws Exception {
        final String absolutePath = projectProvider.getDir("python_path/py_module").getLocation().toPortableString();
        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByPath(suite, absolutePath + "/");

        verifyZeroInteractions(importer);

        verify(detector).libraryDetectedByPath(eq(absolutePath + "/"),
                eq(projectProvider.getFile("python_path/py_module/__init__.py").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.PYTHON, "py_module", projectProvider.getDir("python_path"))));
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void pythonModuleLibraryIsFound_whenPathIsRelativeWithTrailingSeparator() throws Exception {
        final String relativePath = "python_path/py_module";
        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByPath(suite, relativePath + "/");

        verifyZeroInteractions(importer);

        verify(detector).libraryDetectedByPath(eq(relativePath + "/"),
                eq(projectProvider.getFile("python_path/py_module/__init__.py").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.PYTHON, "py_module", projectProvider.getDir("python_path"))));
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void detectingFails_whenPathIsRelativeWithoutTrailingSeparator() throws Exception {
        final String relativePath = "python_path/py_module";
        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByPath(suite, relativePath);

        verifyZeroInteractions(importer);

        verify(detector).libraryDetectingByPathFailed(relativePath, Optional.empty(),
                "The path 'python_path/py_module' should point to either .py file or python module directory.");
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void detectingFails_whenLibraryDoesNotExist() throws Exception {
        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByName(suite, "not_existing");

        verifyZeroInteractions(importer);

        verify(detector).libraryDetectingByNameFailed(eq("not_existing"), eq(Optional.empty()),
                startsWith("RED python session problem"));
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void detectingFails_whenRobotEnvironmentIsInvalid() throws Exception {
        final RobotProject robotProjectSpy = spy(robotProject);
        when(robotProjectSpy.getRuntimeEnvironment()).thenReturn(mock(RobotRuntimeEnvironment.class));

        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProjectSpy, importer, detector);
        locator.locateByName(suite, "unknown");

        verifyZeroInteractions(importer);

        verify(detector).libraryDetectingByNameFailed("unknown", Optional.empty(), "Unable to find 'unknown' library.");
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void detectingFails_whenImportingByNameThrowsException() throws Exception {
        final IResource libResource = projectProvider.getFile("dir_lib.py");
        setupFailedPythonImport("dir_lib");

        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByName(suite, "dir_lib");

        verify(importer).importPythonLib(robotProject.getRuntimeEnvironment(), projectProvider.getProject(),
                robotProject.getRobotProjectConfig(), libResource.getLocation().toFile().getAbsolutePath(), "dir_lib");
        verifyNoMoreInteractions(importer);

        verify(detector).libraryDetectingByNameFailed("dir_lib", Optional.of(libResource.getLocation().toFile()),
                "fail reason");
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void pythonModuleLibraryIsFoundByName() throws Exception {
        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByName(suite, "py_module");

        verifyZeroInteractions(importer);

        verify(detector).libraryDetectedByName(eq("py_module"),
                eq(projectProvider.getFile("python_path/py_module/__init__.py").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.PYTHON, "py_module", projectProvider.getDir("python_path"))));
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void pythonLibraryFromAdditionalPythonPathIsFoundByName() throws Exception {
        final IResource libResource = projectProvider.getFile("python_path/path_lib.py");
        setupPythonImport(libResource, "path_lib");

        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByName(suite, "path_lib");

        verify(importer).importPythonLib(robotProject.getRuntimeEnvironment(), projectProvider.getProject(),
                robotProject.getRobotProjectConfig(), libResource.getLocation().toFile().getAbsolutePath(), "path_lib");
        verifyNoMoreInteractions(importer);

        verify(detector).libraryDetectedByName(eq("path_lib"),
                eq(projectProvider.getFile("python_path/path_lib.py").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.PYTHON, "path_lib", libResource)));
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void pythonLibraryFromCurrentDirIsFoundByName() throws Exception {
        final IResource libResource = projectProvider.getFile("dir_lib.py");
        setupPythonImport(libResource, "dir_lib");

        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByName(suite, "dir_lib");

        verify(importer).importPythonLib(robotProject.getRuntimeEnvironment(), projectProvider.getProject(),
                robotProject.getRobotProjectConfig(), libResource.getLocation().toFile().getAbsolutePath(), "dir_lib");
        verifyNoMoreInteractions(importer);

        verify(detector).libraryDetectedByName(eq("dir_lib"),
                eq(projectProvider.getFile("dir_lib.py").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.PYTHON, "dir_lib", libResource)));
        verifyNoMoreInteractions(detector);
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

        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProjectSpy, importer, detector);
        locator.locateByName(suite, "JavaLib");

        verify(importer).importJavaLib(environment, projectProvider.getProject(), robotProject.getRobotProjectConfig(),
                libResource.getLocation().toFile().getAbsolutePath(), "JavaLib");
        verifyNoMoreInteractions(importer);

        verify(detector).libraryDetectedByName(eq("JavaLib"),
                eq(projectProvider.getFile("JavaLib.jar").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.JAVA, "JavaLib", libResource)));
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void libraryIsImportedFromTheSameFileByPathOnlyOnce() throws Exception {
        final IResource libResource = projectProvider.getFile("python_path/path_lib.py");
        setupPythonImport(libResource);

        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProject, importer, detector);
        locator.locateByPath(suite, "python_path/path_lib.py");
        locator.locateByPath(suite, "./python_path/path_lib.py");
        locator.locateByPath(suite, "../" + PROJECT_NAME + "/python_path/path_lib.py");

        verify(importer).importPythonLib(robotProject.getRuntimeEnvironment(), projectProvider.getProject(),
                robotProject.getRobotProjectConfig(), libResource.getLocation().toFile().getAbsolutePath());
        verifyNoMoreInteractions(importer);

        verify(detector).libraryDetectedByPath(eq("python_path/path_lib.py"),
                eq(projectProvider.getFile("python_path/path_lib.py").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.PYTHON, "path_lib", libResource)));
        verify(detector).libraryDetectedByPath(eq("./python_path/path_lib.py"),
                eq(projectProvider.getFile("python_path/path_lib.py").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.PYTHON, "path_lib", libResource)));
        verify(detector).libraryDetectedByPath(eq("../" + PROJECT_NAME + "/python_path/path_lib.py"),
                eq(projectProvider.getFile("python_path/path_lib.py").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.PYTHON, "path_lib", libResource)));
        verifyNoMoreInteractions(detector);
    }

    @Test
    public void libraryIsImportedFromTheSameFileByNameOnlyOnce() throws Exception {
        final IResource libResource = projectProvider.getFile("JavaLib.jar");
        setupJavaImport(libResource, "NameA");
        setupJavaImport(libResource, "NameB");
        setupJavaImport(libResource, "NameC");

        final RobotRuntimeEnvironment environment = mock(RobotRuntimeEnvironment.class);
        when(environment.getModulePath(eq("NameA"), any(EnvironmentSearchPaths.class)))
                .thenReturn(Optional.of(libResource.getLocation().toFile()));
        when(environment.getModulePath(eq("NameB"), any(EnvironmentSearchPaths.class)))
                .thenReturn(Optional.of(libResource.getLocation().toFile()));
        when(environment.getModulePath(eq("NameC"), any(EnvironmentSearchPaths.class)))
                .thenReturn(Optional.of(libResource.getLocation().toFile()));
        final RobotProject robotProjectSpy = spy(robotProject);
        when(robotProjectSpy.getRuntimeEnvironment()).thenReturn(environment);

        final ReferencedLibraryLocator locator = new ReferencedLibraryLocator(robotProjectSpy, importer, detector);
        locator.locateByName(suite, "NameA");
        locator.locateByName(suite, "NameB");
        locator.locateByName(suite, "NameA");
        locator.locateByName(suite, "NameC");
        locator.locateByName(suite, "NameB");
        locator.locateByName(suite, "NameA");

        verify(importer).importJavaLib(environment, projectProvider.getProject(), robotProject.getRobotProjectConfig(),
                libResource.getLocation().toFile().getAbsolutePath(), "NameA");
        verify(importer).importJavaLib(environment, projectProvider.getProject(), robotProject.getRobotProjectConfig(),
                libResource.getLocation().toFile().getAbsolutePath(), "NameB");
        verify(importer).importJavaLib(environment, projectProvider.getProject(), robotProject.getRobotProjectConfig(),
                libResource.getLocation().toFile().getAbsolutePath(), "NameC");
        verifyNoMoreInteractions(importer);

        verify(detector, times(3)).libraryDetectedByName(eq("NameA"),
                eq(projectProvider.getFile("JavaLib.jar").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.JAVA, "NameA", libResource)));
        verify(detector, times(2)).libraryDetectedByName(eq("NameB"),
                eq(projectProvider.getFile("JavaLib.jar").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.JAVA, "NameB", libResource)));
        verify(detector, times(1)).libraryDetectedByName(eq("NameC"),
                eq(projectProvider.getFile("JavaLib.jar").getLocation().toFile()),
                argThat(isSingleLibrary(LibraryType.JAVA, "NameC", libResource)));
        verifyNoMoreInteractions(detector);
    }

    private void setupJavaImport(final IResource libResource) {
        final String name = libResource.getLocation().removeFileExtension().lastSegment();
        final String path = libResource.getLocation().toPortableString();
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.JAVA, name, path);
        when(importer.importJavaLib(any(RobotRuntimeEnvironment.class), any(IProject.class),
                any(RobotProjectConfig.class), anyString(), eq(name))).thenReturn(Collections.singletonList(lib));
    }

    private void setupJavaImport(final IResource libResource, final String name) {
        final String path = libResource.getLocation().toPortableString();
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.JAVA, name, path);
        when(importer.importJavaLib(any(RobotRuntimeEnvironment.class), any(IProject.class),
                any(RobotProjectConfig.class), anyString(), eq(name))).thenReturn(Collections.singletonList(lib));
    }

    private void setupPythonImport(final IResource libResource) {
        final String name = libResource.getLocation().removeFileExtension().lastSegment();
        final String path = libResource.getLocation().toPortableString();
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, name, path);
        when(importer.importPythonLib(any(RobotRuntimeEnvironment.class), any(IProject.class),
                any(RobotProjectConfig.class), anyString())).thenReturn(Collections.singletonList(lib));
    }

    private void setupPythonImport(final IResource libResource, final String name) {
        final String path = libResource.getLocation().toPortableString();
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, name, path);
        when(importer.importPythonLib(any(RobotRuntimeEnvironment.class), any(IProject.class),
                any(RobotProjectConfig.class), anyString(), eq(name))).thenReturn(Collections.singletonList(lib));
    }

    private void setupFailedPythonImport() {
        when(importer.importPythonLib(any(RobotRuntimeEnvironment.class), any(IProject.class),
                any(RobotProjectConfig.class), anyString())).thenThrow(new RobotEnvironmentException("fail reason"));
    }

    private void setupFailedPythonImport(final String name) {
        when(importer.importPythonLib(any(RobotRuntimeEnvironment.class), any(IProject.class),
                any(RobotProjectConfig.class), anyString(), eq(name)))
                        .thenThrow(new RobotEnvironmentException("fail reason"));
    }

    static ArgumentMatcher<Collection<ReferencedLibrary>> isSingleLibrary(final LibraryType type, final String name,
            final IResource resource) {
        return toMatch -> {
            final Optional<ReferencedLibrary> first = toMatch.stream().findFirst();
            return toMatch.size() == 1 && first.isPresent() && Objects.equal(type.toString(), first.get().getType())
                    && Objects.equal(name, first.get().getName())
                    && Objects.equal(resource.getLocation().toPortableString(), first.get().getPath());
        };
    }

}
