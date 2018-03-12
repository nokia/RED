/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.project.library.Libraries.createRefLibs;
import static org.robotframework.ide.eclipse.main.plugin.project.library.Libraries.createRemoteLib;
import static org.robotframework.ide.eclipse.main.plugin.project.library.Libraries.createStdLibs;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

public class RobotSuiteFileTest {

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(RobotSuiteFileTest.class);

    private RobotModel robotModel;

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
}
