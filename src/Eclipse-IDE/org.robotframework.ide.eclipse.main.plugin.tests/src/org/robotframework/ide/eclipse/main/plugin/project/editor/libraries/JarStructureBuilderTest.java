/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.assertj.core.api.Condition;
import org.eclipse.core.runtime.IPath;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder.JarClass;
import org.robotframework.red.junit.ProjectProvider;

@RunWith(MockitoJUnitRunner.class)
public class JarStructureBuilderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(JarStructureBuilderTest.class);

    @Mock
    private RobotRuntimeEnvironment environment;

    private RobotProjectConfig config;

    private URI moduleLocation;

    @Before
    public void before() throws Exception {
        config = new RobotProjectConfig();
        moduleLocation = projectProvider.createFile("module.jar").getLocationURI();
    }

    @Test
    public void testGettingPythonClassesFromJarByPath() throws Exception {
        final JarStructureBuilder builder = new JarStructureBuilder(environment, config, projectProvider.getProject());

        builder.provideEntriesFromFile(moduleLocation);

        verify(environment).getClassesFromModule(new File(moduleLocation), new EnvironmentSearchPaths());
    }

    @Test
    public void testGettingPythonClassesFromJarByFile() throws Exception {
        final JarStructureBuilder builder = new JarStructureBuilder(environment, config, projectProvider.getProject());

        builder.provideEntriesFromFile(moduleLocation);

        verify(environment).getClassesFromModule(new File(moduleLocation), new EnvironmentSearchPaths());
    }

    @Test
    public void notJarFilesAreNotProcessed() throws Exception {
        final JarStructureBuilder builder = new JarStructureBuilder(environment, config, projectProvider.getProject());

        builder.provideEntriesFromFile(projectProvider.createFile("module.other").getLocationURI());

        verifyZeroInteractions(environment);

    }

    @Test
    public void testGettingPythonClassesFromJarWithAdditionalSearchPaths() throws Exception {
        config.addPythonPath(SearchPath.create("path1"));
        config.addPythonPath(SearchPath.create("path2"));
        config.addClassPath(SearchPath.create("path3"));

        final JarStructureBuilder builder = new JarStructureBuilder(environment, config, projectProvider.getProject());

        builder.provideEntriesFromFile(moduleLocation);

        verify(environment).getClassesFromModule(new File(moduleLocation),
                new RedEclipseProjectConfig(projectProvider.getProject(), config)
                        .createAdditionalEnvironmentSearchPaths());
    }

    @Test
    public void javaEntriesFromJarFileAreProvided() throws Exception {
        final File jarFile = projectProvider.createFile("lib.jar").getLocation().toFile();
        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(jarFile))) {
            zipStream.putNextEntry(new ZipEntry("JavaClass.class"));
            zipStream.putNextEntry(new ZipEntry("A/JavaClass.class"));
            zipStream.putNextEntry(new ZipEntry("A/B/JavaClass.class"));
            zipStream.putNextEntry(new ZipEntry("file.txt"));
        }

        final JarStructureBuilder builder = new JarStructureBuilder(environment, config, projectProvider.getProject());

        final Collection<ILibraryClass> classes = builder.provideEntriesFromFile(jarFile.toURI());

        assertThat(classes.stream().map(ILibraryClass::getQualifiedName)).containsExactly("JavaClass", "A.JavaClass",
                "A.B.JavaClass");
    }

    @Test
    public void pythonEntriesFromJarFileAreProvided() throws Exception {
        when(environment.getClassesFromModule(new File(moduleLocation), new EnvironmentSearchPaths()))
                .thenReturn(newArrayList("module", "module.ClassName", "module.ClassName.ClassName"));

        final JarStructureBuilder builder = new JarStructureBuilder(environment, config, projectProvider.getProject());

        final Collection<ILibraryClass> classes = builder.provideEntriesFromFile(moduleLocation);

        assertThat(classes.stream().map(ILibraryClass::getQualifiedName)).containsExactly("module", "module.ClassName");
    }

    @Test
    public void jarClassesAreCreatedFromZipEntry() throws Exception {
        assertThat(JarClass.createFromZipJavaEntry("name.class")).isEqualTo(new JarClass("name"));
        assertThat(JarClass.createFromZipJavaEntry("dirA/name.class")).isEqualTo(new JarClass("dirA.name"));
        assertThat(JarClass.createFromZipJavaEntry("dirA/dirB/name.class")).isEqualTo(new JarClass("dirA.dirB.name"));
    }

    @Test
    public void referenceLibraryIsCreated() throws Exception {
        final ILibraryClass libClass = new JarClass("Java.ClassName");
        final IPath projectLocation = projectProvider.getProject().getLocation();
        final String fullLibraryPath = projectLocation.append("path/to/file.jar").toOSString();
        final ReferencedLibrary lib = libClass.toReferencedLibrary(fullLibraryPath);

        assertThat(lib).has(sameFieldsAs(ReferencedLibrary.create(LibraryType.JAVA, "Java.ClassName",
                projectProvider.getProject().getName() + "/path/to/file.jar")));
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
