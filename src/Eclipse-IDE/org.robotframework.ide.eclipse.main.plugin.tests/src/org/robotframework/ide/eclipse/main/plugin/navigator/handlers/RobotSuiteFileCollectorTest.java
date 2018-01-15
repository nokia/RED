/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class RobotSuiteFileCollectorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotSuiteFileCollectorTest.class);

    private static RobotModel model;

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
        model = RedPlugin.getModelManager().getModel();

        topLevelDirectory = projectProvider.createDir("dir1");
        nestedDirectory = projectProvider.createDir("dir1/dir2");

        topLevelFile = model.createSuiteFile(projectProvider.createFile("file1.robot", "*** Keywords ***"));
        initFile = model.createSuiteFile(projectProvider.createFile("dir1/__init__.robot", "*** Settings ***"));
        tsvFile = model.createSuiteFile(projectProvider.createFile("dir1/file2.tsv", "*** Keywords ***"));
        txtFile = model.createSuiteFile(projectProvider.createFile("dir1/file3.txt", "*** Test Cases ***"));
        robotFile = model.createSuiteFile(projectProvider.createFile("dir1/dir2/file4.robot", "*** Test Cases ***"));

        notRobotFile = projectProvider.createFile("lib.py", "def kw():", "  pass");

        projectProvider.configure();
    }

    @AfterClass
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void onlySelectedFileShouldBeCollected() throws Exception {
        final List<IResource> selectedResources = newArrayList(topLevelFile.getFile(), initFile.getFile());

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources,
                new NullProgressMonitor());
        assertThat(files).containsOnly(topLevelFile, initFile);
    }

    @Test
    public void allFilesFromDirectoryShouldBeCollected() throws Exception {
        final List<IResource> selectedResources = newArrayList(topLevelDirectory);

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources,
                new NullProgressMonitor());
        assertThat(files).containsOnly(initFile, tsvFile, txtFile, robotFile);
    }

    @Test
    public void allFilesFromProjectShouldBeCollected() throws Exception {
        final List<IResource> selectedResources = newArrayList(projectProvider.getProject());

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources,
                new NullProgressMonitor());
        assertThat(files).containsOnly(topLevelFile, initFile, tsvFile, txtFile, robotFile);
    }

    @Test
    public void filesShouldBeCollectedOnlyOnce() throws Exception {
        final List<IResource> selectedResources = newArrayList(topLevelDirectory, tsvFile.getFile(),
                robotFile.getFile());

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources,
                new NullProgressMonitor());
        assertThat(files).containsOnly(initFile, tsvFile, txtFile, robotFile);
    }

    @Test
    public void filesAndDirectoriesCanBeMixed() throws Exception {
        final List<IResource> selectedResources = newArrayList(nestedDirectory, topLevelFile.getFile());

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources,
                new NullProgressMonitor());
        assertThat(files).containsOnly(topLevelFile, robotFile);
    }

    @Test
    public void notRobotFilesShouldBeIgnored() throws Exception {
        final List<IResource> selectedResources = newArrayList(notRobotFile, topLevelFile.getFile());

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources,
                new NullProgressMonitor());
        assertThat(files).containsOnly(topLevelFile);
    }

    @Test
    public void excludedFilesShouldBeIgnored() throws Exception {
        excludePathInProjectConfig("dir1");

        final List<IResource> selectedResources = newArrayList(topLevelDirectory, topLevelFile.getFile());

        final Set<RobotSuiteFile> files = RobotSuiteFileCollector.collectFiles(selectedResources,
                new NullProgressMonitor());
        assertThat(files).containsOnly(topLevelFile);

        includePathInProjectConfig("dir1");
    }

    private void excludePathInProjectConfig(final String path) throws Exception {
        final RobotProjectConfig config = model.createRobotProject(projectProvider.getProject())
                .getRobotProjectConfig();
        config.addExcludedPath(path);
    }

    private void includePathInProjectConfig(final String path) {
        final RobotProjectConfig config = model.createRobotProject(projectProvider.getProject())
                .getRobotProjectConfig();
        config.removeExcludedPath(path);
    }
}
