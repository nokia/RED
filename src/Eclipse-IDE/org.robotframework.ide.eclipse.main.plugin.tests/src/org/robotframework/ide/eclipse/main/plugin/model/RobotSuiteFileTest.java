/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries.createRefLibs;
import static org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries.createRemoteLib;
import static org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries.createStdLibs;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

public class RobotSuiteFileTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotSuiteFileTest.class);

    private RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.configure();
        projectProvider.createDir("res");
        projectProvider.createDir("res/a");
        projectProvider.createDir("res/a/b");
        projectProvider.createDir("res/a/b/c");
        projectProvider.createFile("res/res1.robot");
        projectProvider.createFile("res/a/res2.robot");
        projectProvider.createFile("res/a/b/res3.robot");
        projectProvider.createFile("res/a/b/c/res4.robot");
    }

    @Before
    public void beforeTest() throws Exception {
        robotModel = new RobotModel();
    }

    @After
    public void afterTest() {
        robotModel = null;
    }
    
    @Test
    public void librarySpecsAreReturned_whenSuiteImportsLibrariesByName() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot",
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

    @SuppressWarnings("unchecked")
    @Test
    public void librarySpecsAreReturned_whenSuiteImportsMultipleDifferentRemoteLibraries() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot",
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
        assertThat(files).containsExactly(projectProvider.getFile("res/res1.robot"),
                projectProvider.getFile("res/a/res2.robot"));
    }

    @Test
    public void resourcesAreReturnedInImportOrder_whenImportingMultipleFiles() throws Exception {
        final String[] importSection = createResourceImportSection("a/res2.robot", "res1.robot", "a/b/res3.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("res/test.robot", importSection);

        final List<IResource> files = suiteFile.getImportedResources();
        assertThat(files).containsExactly(projectProvider.getFile("res/a/res2.robot"),
                projectProvider.getFile("res/res1.robot"), projectProvider.getFile("res/a/b/res3.robot"));
    }

    @Test
    public void resourcesAreReturned_whenVariableIsUsedInImportPath() throws Exception {
        final String[] importSection = createResourceImportSection("${execdir}/res/res1.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("res/test.robot", importSection);

        final List<IResource> files = suiteFile.getImportedResources();
        assertThat(files).containsExactly(projectProvider.getFile("res/res1.robot"));
    }

    @Test
    public void resourcesAreReturned_whenAbsoluteImportPathIsUsed() throws Exception {
        final String[] importSection = createResourceImportSection(
                projectProvider.getProject().getLocation().toString() + "/res/a/res2.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("res/test.robot", importSection);

        final List<IResource> files = suiteFile.getImportedResources();
        assertThat(files).containsExactly(projectProvider.getFile("res/a/res2.robot"));
    }

    @Test
    public void resourcesAreReturned_whenRelativeImportPathIsUsed() throws Exception {
        final String[] importSection = createResourceImportSection("../../res1.robot", "../res2.robot", "./res3.robot",
                "c/res4.robot");
        final RobotSuiteFile suiteFile = createSuiteFile("res/a/b/test.robot", importSection);

        final List<IResource> files = suiteFile.getImportedResources();
        assertThat(files).containsExactly(projectProvider.getFile("res/res1.robot"),
                projectProvider.getFile("res/a/res2.robot"), projectProvider.getFile("res/a/b/res3.robot"),
                projectProvider.getFile("res/a/b/c/res4.robot"));
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
        final IFile sourceFile = projectProvider.createFile(filePath, lines);
        return new RobotModel().createSuiteFile(sourceFile);
    }
}
