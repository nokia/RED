/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Objects;

import org.assertj.core.api.Condition;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder.PythonClass;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class PythonLibStructureBuilderTest {

    @Project(files = { "module.py" })
    static IProject project;

    private IRuntimeEnvironment environment;

    private RobotProjectConfig config;

    private URI moduleLocation;

    @BeforeEach
    public void before() throws Exception {
        environment = mock(IRuntimeEnvironment.class);
        config = new RobotProjectConfig();
        moduleLocation = getFile(project, "module.py").getLocationURI();
    }

    @Test
    public void testGettingPythonClassesFromModule() throws Exception {
        final PythonLibStructureBuilder builder = new PythonLibStructureBuilder(environment, config,
                project);

        builder.provideEntriesFromFile(moduleLocation);

        verify(environment).getClassesFromModule(new File(moduleLocation), new EnvironmentSearchPaths());
    }

    @Test
    public void testGettingAllPythonClassesFromModule() throws Exception {
        final PythonLibStructureBuilder builder = new PythonLibStructureBuilder(environment, config,
                project);

        builder.provideAllEntriesFromFile(moduleLocation);

        verify(environment).getClassesFromModule(new File(moduleLocation), new EnvironmentSearchPaths());
    }

    @Test
    public void testGettingPythonClassesFromModuleWithAdditionalSearchPaths() throws Exception {
        config.addPythonPath(SearchPath.create("path1"));
        config.addPythonPath(SearchPath.create("path2"));
        config.addClassPath(SearchPath.create("path3"));

        final PythonLibStructureBuilder builder = new PythonLibStructureBuilder(environment, config,
                project);

        builder.provideEntriesFromFile(moduleLocation);

        verify(environment).getClassesFromModule(new File(moduleLocation),
                new RedEclipseProjectConfig(project, config)
                        .createAdditionalEnvironmentSearchPaths());
    }

    @Test
    public void entriesWithoutDuplicatedFileAndClassNameAreProvided() throws Exception {
        when(environment.getClassesFromModule(new File(moduleLocation), new EnvironmentSearchPaths()))
                .thenReturn(newArrayList("module", "module.ClassName", "module.ClassName.ClassName",
                        "module.OtherClassName", "module.OtherClassName.OtherClassName"));

        final PythonLibStructureBuilder builder = new PythonLibStructureBuilder(environment, config,
                project);

        final Collection<ILibraryClass> classes = builder.provideEntriesFromFile(moduleLocation);

        assertThat(classes.stream().map(ILibraryClass::getQualifiedName)).containsExactly("module", "module.ClassName",
                "module.OtherClassName");
    }

    @Test
    public void allEntriesAreProvidedWithCorrectNameAndType() throws Exception {
        when(environment.getClassesFromModule(new File(moduleLocation), new EnvironmentSearchPaths()))
                .thenReturn(newArrayList("module", "module.ClassName", "module.ClassName.ClassName",
                        "module.OtherClassName", "module.OtherClassName.OtherClassName"));

        final PythonLibStructureBuilder builder = new PythonLibStructureBuilder(environment, config,
                project);

        final Collection<ILibraryClass> classes = builder.provideAllEntriesFromFile(moduleLocation);

        assertThat(classes.stream().map(ILibraryClass::getQualifiedName)).containsExactly("module", "module.ClassName",
                "module.ClassName.ClassName", "module.OtherClassName", "module.OtherClassName.OtherClassName");
        assertThat(classes.stream().map(ILibraryClass::getType)).containsExactly(LibraryType.PYTHON, LibraryType.PYTHON,
                LibraryType.PYTHON, LibraryType.PYTHON, LibraryType.PYTHON);
    }

    @Test
    public void pythonClassesAreCreatedWithoutDuplicatedClassName() throws Exception {
        assertThat(PythonClass.createWithoutDuplicationOfFileAndClassName("simpleName"))
                .isEqualTo(new PythonClass("simpleName"));
        assertThat(PythonClass.createWithoutDuplicationOfFileAndClassName("className.otherName"))
                .isEqualTo(new PythonClass("className.otherName"));
        assertThat(PythonClass.createWithoutDuplicationOfFileAndClassName("mod.className.otherName"))
                .isEqualTo(new PythonClass("mod.className.otherName"));
        assertThat(PythonClass.createWithoutDuplicationOfFileAndClassName("className.className"))
                .isEqualTo(new PythonClass("className"));
        assertThat(PythonClass.createWithoutDuplicationOfFileAndClassName("mod.className.className"))
                .isEqualTo(new PythonClass("mod.className"));
    }

    @Test
    public void referenceLibraryIsCreatedForPythonFile() throws Exception {
        final ILibraryClass libClass = new PythonClass("libName");
        final IPath projectLocation = project.getLocation();
        final String fullLibraryPath = projectLocation.append("folder/libName.py").toOSString();
        final ReferencedLibrary lib = libClass.toReferencedLibrary(fullLibraryPath);

        assertThat(lib).has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "libName",
                project.getName() + "/folder/libName.py")));
    }

    @Test
    public void referenceLibraryIsCreatedForPythonFile_withQualifiedName() throws Exception {
        final ILibraryClass libClass = new PythonClass("nameA.nameB.libName");
        final IPath projectLocation = project.getLocation();
        final String fullLibraryPath = projectLocation.append("folder/nameA/nameB/libName.py").toOSString();
        final ReferencedLibrary lib = libClass.toReferencedLibrary(fullLibraryPath);

        assertThat(lib).has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "nameA.nameB.libName",
                project.getName() + "/folder/nameA/nameB/libName.py")));
    }

    @Test
    public void referenceLibraryIsCreatedForPythonModule() throws Exception {
        final ILibraryClass libClass = new PythonClass("moduleName");
        final IPath projectLocation = project.getLocation();
        final String fullLibraryPath = projectLocation.append("folder/moduleName/__init__.py").toOSString();
        final ReferencedLibrary lib = libClass.toReferencedLibrary(fullLibraryPath);

        assertThat(lib).has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "moduleName",
                project.getName() + "/folder/moduleName/__init__.py")));
    }

    @Test
    public void referenceLibraryIsCreatedForPythonModule_withQualifiedName() throws Exception {
        final ILibraryClass libClass = new PythonClass("nameA.nameB.moduleName");
        final IPath projectLocation = project.getLocation();
        final String fullLibraryPath = projectLocation.append("folder/nameA/nameB/moduleName/__init__.py").toOSString();
        final ReferencedLibrary lib = libClass.toReferencedLibrary(fullLibraryPath);

        assertThat(lib).has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "nameA.nameB.moduleName",
                project.getName() + "/folder/nameA/nameB/moduleName/__init__.py")));
    }

    private static Condition<? super ReferencedLibrary> sameFieldsAs(final ReferencedLibrary library) {
        return new Condition<ReferencedLibrary>() {

            @Override
            public boolean matches(final ReferencedLibrary toMatch) {
                return Objects.equals(library.getType(), toMatch.getType())
                        && Objects.equals(library.getName(), toMatch.getName())
                        && Objects.equals(library.getPath(), toMatch.getPath());
            }
        };
    }

}
