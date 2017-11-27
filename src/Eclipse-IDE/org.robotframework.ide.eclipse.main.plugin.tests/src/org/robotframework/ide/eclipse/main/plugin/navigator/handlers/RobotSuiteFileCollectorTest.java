/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class RobotSuiteFileCollectorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotSuiteFileCollectorTest.class);

    private static IFolder topLevelDirectory;

    private static IFolder nestedDirectory;

    private static RobotSuiteFile topLevelFile;

    private static RobotSuiteFile initFile;

    private static RobotSuiteFile tsvFile;

    private static RobotSuiteFile txtFile;

    private static RobotSuiteFile robotFile;

    private static IFile notRobotFile;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        topLevelDirectory = projectProvider.createDir("dir1");
        nestedDirectory = projectProvider.createDir("dir1/dir2");

        final RobotModel model = new RobotModel();
        topLevelFile = model.createSuiteFile(projectProvider.createFile("file1.robot", "*** Keywords ***"));
        initFile = model.createSuiteFile(projectProvider.createFile("dir1/__init__.robot", "*** Settings ***"));
        tsvFile = model.createSuiteFile(projectProvider.createFile("dir1/file2.tsv", "*** Keywords ***"));
        txtFile = model.createSuiteFile(projectProvider.createFile("dir1/file3.txt", "*** Test Cases ***"));
        robotFile = model.createSuiteFile(projectProvider.createFile("dir1/dir2/file4.robot", "*** Test Cases ***"));

        notRobotFile = projectProvider.createFile("lib.py", "def kw():", "  pass");
    }

    @Test
    public void onlySelectedFileShouldBeCollected() throws Exception {
        final List<IResource> selectedResources = new ArrayList<>();
        selectedResources.add(topLevelFile.getFile());
        selectedResources.add(initFile.getFile());

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources);
        assertThat(files).containsOnly(topLevelFile, initFile);
    }

    @Test
    public void allFilesFromDirectoryShouldBeCollected() throws Exception {
        final List<IResource> selectedResources = new ArrayList<>();
        selectedResources.add(topLevelDirectory);

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources);
        assertThat(files).containsOnly(initFile, tsvFile, txtFile, robotFile);
    }

    @Test
    public void allFilesFromProjectShouldBeCollected() throws Exception {
        final List<IResource> selectedResources = new ArrayList<>();
        selectedResources.add(projectProvider.getProject());

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources);
        assertThat(files).containsOnly(topLevelFile, initFile, tsvFile, txtFile, robotFile);
    }

    @Test
    public void filesShouldBeCollectedOnlyOnce() throws Exception {
        final List<IResource> selectedResources = new ArrayList<>();
        selectedResources.add(topLevelDirectory);
        selectedResources.add(tsvFile.getFile());
        selectedResources.add(robotFile.getFile());

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources);
        assertThat(files).containsOnly(initFile, tsvFile, txtFile, robotFile);
    }

    @Test
    public void filesAndDirectoriesCanBeMixed() throws Exception {
        final List<IResource> selectedResources = new ArrayList<>();
        selectedResources.add(nestedDirectory);
        selectedResources.add(topLevelFile.getFile());

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources);
        assertThat(files).containsOnly(topLevelFile, robotFile);
    }

    @Test
    public void notRobotFilesShouldBeIgnored() throws Exception {
        final List<IResource> selectedResources = new ArrayList<>();
        selectedResources.add(notRobotFile);
        selectedResources.add(topLevelFile.getFile());

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources);
        assertThat(files).containsOnly(topLevelFile);
    }
}
