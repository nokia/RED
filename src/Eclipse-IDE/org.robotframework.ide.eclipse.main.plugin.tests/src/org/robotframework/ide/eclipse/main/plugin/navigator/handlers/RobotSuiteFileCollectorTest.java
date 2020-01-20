/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.configure;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createDir;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RobotSuiteFileCollectorTest {

    @Project
    static IProject project;

    private static RobotModel model;

    private static IFolder topLevelDirectory;

    private static IFolder nestedDirectory;

    private static RobotSuiteFile topLevelFile;

    private static RobotSuiteFile initFile;

    private static RobotSuiteFile tsvFile;

    private static RobotSuiteFile txtFile;

    private static RobotSuiteFile robotFile;

    private static IFile notRobotFile;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        model = RedPlugin.getModelManager().getModel();

        topLevelDirectory = createDir(project, "dir1");
        nestedDirectory = createDir(project, "dir1/dir2");

        topLevelFile = model.createSuiteFile(createFile(project, "file1.robot", "*** Keywords ***"));
        initFile = model.createSuiteFile(createFile(project, "dir1/__init__.robot", "*** Settings ***"));
        tsvFile = model.createSuiteFile(createFile(project, "dir1/file2.tsv", "*** Keywords ***"));
        txtFile = model.createSuiteFile(createFile(project, "dir1/file3.txt", "*** Test Cases ***"));
        robotFile = model.createSuiteFile(createFile(project, "dir1/dir2/file4.robot", "*** Test Cases ***"));

        notRobotFile = createFile(project, "lib.py", "def kw():", "  pass");
    }

    @AfterAll
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
    }

    @BeforeEach
    public void before() throws Exception {
        configure(project);
    }

    @Test
    public void onlySelectedFileShouldBeCollected() throws Exception {
        final List<RobotSuiteFile> suites = collectSuites(topLevelFile.getFile(), initFile.getFile());

        assertThat(suites).containsExactly(topLevelFile, initFile);
    }

    @Test
    public void allFilesFromDirectoryShouldBeCollected() throws Exception {
        final List<RobotSuiteFile> suites = collectSuites(topLevelDirectory);

        assertThat(suites).containsExactly(initFile, robotFile, tsvFile, txtFile);
    }

    @Test
    public void allFilesFromProjectShouldBeCollected() throws Exception {
        final List<RobotSuiteFile> suites = collectSuites(project);

        assertThat(suites).containsExactly(initFile, robotFile, tsvFile, txtFile, topLevelFile);
    }

    @Test
    public void filesShouldBeCollectedOnlyOnce() throws Exception {
        final List<RobotSuiteFile> suites = collectSuites(topLevelDirectory, tsvFile.getFile(), robotFile.getFile());

        assertThat(suites).containsExactly(initFile, robotFile, tsvFile, txtFile);
    }

    @Test
    public void filesAndDirectoriesCanBeMixed() throws Exception {
        final List<RobotSuiteFile> suites = collectSuites(nestedDirectory, topLevelFile.getFile());

        assertThat(suites).containsExactly(robotFile, topLevelFile);
    }

    @Test
    public void notRobotFilesShouldBeIgnored() throws Exception {
        final List<RobotSuiteFile> suites = collectSuites(notRobotFile, topLevelFile.getFile());

        assertThat(suites).containsExactly(topLevelFile);
    }

    @Test
    public void excludedFilesShouldBeIgnored() throws Exception {
        final RobotProjectConfig config = model.createRobotProject(project).getRobotProjectConfig();
        config.addExcludedPath("dir1");

        final List<RobotSuiteFile> suites = collectSuites(topLevelDirectory, topLevelFile.getFile());

        assertThat(suites).containsExactly(topLevelFile);
    }

    private List<RobotSuiteFile> collectSuites(final IResource... resources) throws InterruptedException {
        final List<RobotSuiteFile> result = new ArrayList<>();
        final WorkspaceJob job = RobotSuiteFileCollector.createCollectingJob(newArrayList(resources), result::addAll);
        job.schedule();
        job.join();
        return result;
    }
}
