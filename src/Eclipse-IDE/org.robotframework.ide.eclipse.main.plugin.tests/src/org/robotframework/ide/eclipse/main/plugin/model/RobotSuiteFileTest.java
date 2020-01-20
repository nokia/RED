/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries.createRefLibs;
import static org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries.createRemoteLib;
import static org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries.createStdLibs;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

@ExtendWith(ProjectExtension.class)
public class RobotSuiteFileTest {

    @Project(createDefaultRedXml = true, dirs = { "res", "res/a", "res/a/b", "res/a/b/c" }, files = { "res/res1.robot",
            "res/a/res2.robot", "res/a/b/res3.robot", "res/a/b/c/res4.robot" })
    static IProject project;

    private RobotModel robotModel;

    @BeforeEach
    public void beforeTest() throws Exception {
        robotModel = new RobotModel();
    }

    @AfterEach
    public void afterTest() {
        robotModel = null;
    }

    @Test
    public void librarySpecsAreReturned_whenSuiteImportsLibrariesByName() throws Exception {
        final IFile file = createFile(project, "suite.robot",
                "*** Settings ***",
                "Library",
                "Library  Collections",
                "Library  myLib");
        final RobotSuiteFile fileModel = robotModel.createSuiteFile(file);

        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        robotProject.setStandardLibraries(createStdLibs("Collections", "OperatingSystem"));
        robotProject.setReferencedLibraries(createRefLibs("myLib", "myLib2"));

        final Multimap<LibrarySpecification, Optional<String>> imported = fileModel.getImportedLibraries();

        assertThat(imported.keySet()).hasSize(2);
        assertThat(imported.keySet().stream().map(LibrarySpecification::getName)).containsOnly("Collections", "myLib");
    }

    @Test
    public void librarySpecsAreReturned_whenSuiteImportsMultipleDifferentRemoteLibraries() throws Exception {
        final IFile file = createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  Remote  http://1.2.3.4/mylib",
                "Library  Remote  http://5.6.7.8/mylib2");
        final RobotSuiteFile fileModel = robotModel.createSuiteFile(file);

        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        final ImmutableMap<LibraryDescriptor, LibrarySpecification> stdLibs = ImmutableMap
                .<LibraryDescriptor, LibrarySpecification> builder()
                .putAll(createRemoteLib("http://1.2.3.4/mylib"))
                .putAll(createRemoteLib("http://5.6.7.8/mylib2"))
                .build();
        robotProject.setStandardLibraries(stdLibs);

        final Multimap<LibrarySpecification, Optional<String>> imported = fileModel.getImportedLibraries();

        assertThat(imported.keySet()).hasSize(2);
        assertThat(imported.keySet().stream().map(LibrarySpecification::getName)).containsOnly("Remote");
        assertThat(imported.keySet().stream().map(LibrarySpecification::getDescriptor).map(
                LibraryDescriptor::getArguments)).containsOnly(newArrayList("http://1.2.3.4/mylib"),
                        newArrayList("http://5.6.7.8/mylib2"));
    }

    @Test
    public void noResourcesAreReturned_whenThereAreNoImports() throws Exception {
        final String[] importSection = createResourceImportSection();
        final RobotSuiteFile suiteFile = createSuiteFile("res/test.robot", importSection);

        final List<IResource> files = suiteFile.getImportedResources();
        assertThat(files).isEmpty();
    }

    @Test
    public void onlyExistingResourcesAreReturned_whenThereAreImportsForNonExistingFilesToo() throws Exception {
        final String[] importSection = createResourceImportSection("res1.robot", "not_existing.robot", "a/res2.robot",
                "xyz.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("res/test.robot", importSection);

        final List<IResource> files = suiteFile.getImportedResources();
        assertThat(files).containsExactly(getFile(project, "res/res1.robot"), getFile(project, "res/a/res2.robot"));
    }

    @Test
    public void resourcesAreReturnedInImportOrder_whenImportingMultipleFiles() throws Exception {
        final String[] importSection = createResourceImportSection("a/res2.robot", "res1.robot", "a/b/res3.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("res/test.robot", importSection);

        final List<IResource> files = suiteFile.getImportedResources();
        assertThat(files).containsExactly(getFile(project, "res/a/res2.robot"), getFile(project, "res/res1.robot"),
                getFile(project, "res/a/b/res3.robot"));
    }

    @Test
    public void resourcesAreReturned_whenVariableIsUsedInImportPath() throws Exception {
        final String[] importSection = createResourceImportSection("${execdir}/res/res1.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("res/test.robot", importSection);

        final List<IResource> files = suiteFile.getImportedResources();
        assertThat(files).containsExactly(getFile(project, "res/res1.robot"));
    }

    @Test
    public void resourcesAreReturned_whenAbsoluteImportPathIsUsed() throws Exception {
        final String[] importSection = createResourceImportSection(
                project.getLocation().toString() + "/res/a/res2.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("res/test.robot", importSection);

        final List<IResource> files = suiteFile.getImportedResources();
        assertThat(files).containsExactly(getFile(project, "res/a/res2.robot"));
    }

    @Test
    public void resourcesAreReturned_whenRelativeImportPathIsUsed() throws Exception {
        final String[] importSection = createResourceImportSection("../../res1.robot", "../res2.robot", "./res3.robot",
                "c/res4.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("res/a/b/test.robot", importSection);

        final List<IResource> files = suiteFile.getImportedResources();
        assertThat(files).containsExactly(getFile(project, "res/res1.robot"), getFile(project, "res/a/res2.robot"),
                getFile(project, "res/a/b/res3.robot"), getFile(project, "res/a/b/c/res4.robot"));
    }

    @Test
    public void robotEnvironmentIsReturned() throws Exception {
        final IFile file = getFile(project, "res/res1.robot");
        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        final RobotSuiteFile suiteFile = new RobotSuiteFile(robotProject, file);
        assertThat(suiteFile.getRuntimeEnvironment()).isExactlyInstanceOf(RobotRuntimeEnvironment.class);
    }

    @Test
    public void robotParserFileIsReturned_whenLocationIsNotNull() throws Exception {
        final IFile file = getFile(project, "res/res1.robot");
        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        final RobotSuiteFile suiteFile = new RobotSuiteFile(robotProject, file);
        assertThat(suiteFile.getRobotParserFile()).hasName("res1.robot");
    }

    @Test
    public void robotParserFileIsReturned_whenLocationIsNull() throws Exception {
        final IFile file = mock(IFile.class);
        when(file.getLocation()).thenReturn(null);
        when(file.getName()).thenReturn("abc.robot");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotSuiteFile suiteFile = new RobotSuiteFile(robotProject, file);
        assertThat(suiteFile.getRobotParserFile()).hasName("abc.robot");
    }

    private static String[] createResourceImportSection(final String... resourcePaths) {
        final String[] result = new String[resourcePaths.length + 1];
        result[0] = "*** Settings ***";
        for (int i = 0; i < resourcePaths.length; i++) {
            result[i + 1] = "Resource  " + resourcePaths[i];
        }
        return result;
    }

    private static RobotSuiteFile createSuiteFile(final String filePath, final String... lines)
            throws IOException, CoreException {
        final IFile sourceFile = createFile(project, filePath, lines);
        return new RobotModel().createSuiteFile(sourceFile);
    }
}
