/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace.Paths;
import org.robotframework.red.junit.ProjectProvider;

public class RedWorkspaceTest {

    private static final String PROJECT_NAME = RedWorkspaceTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ProjectProvider outsideProjectProvider = new ProjectProvider(PROJECT_NAME + "_OUTSIDE");

    private static IProject project;

    private static IFolder workspaceDir;

    private static IFile workspaceFile;

    private static File nonWorkspaceDir;

    private static File nonWorkspaceFile;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        project = projectProvider.getProject();
        workspaceDir = projectProvider.createDir("folder");
        workspaceFile = projectProvider.createFile("folder/file.txt");
        nonWorkspaceDir = tempFolder.newFolder("nonWorkspaceFolder");
        nonWorkspaceFile = tempFolder.newFile("nonWorkspaceFolder/nonWorkspaceFile.txt");
    }

    @Test
    public void workspaceRelativePathIsCreated_whenPathIsFromWorkspace() throws Exception {
        assertThat(Paths.toWorkspaceRelativeIfPossible(new Path(workspaceDir.getLocation().toOSString())))
                .isEqualTo(new Path(PROJECT_NAME + "/folder"));
        assertThat(Paths.toWorkspaceRelativeIfPossible(new Path(workspaceFile.getLocation().toOSString())))
                .isEqualTo(new Path(PROJECT_NAME + "/folder/file.txt"));
    }

    @Test
    public void workspaceRelativePathIsNotCreated_whenPathIsOutOfWorkspace() throws Exception {
        assertThat(Paths.toWorkspaceRelativeIfPossible(new Path(nonWorkspaceDir.getAbsolutePath())))
                .isEqualTo(new Path(nonWorkspaceDir.getAbsolutePath()));
        assertThat(Paths.toWorkspaceRelativeIfPossible(new Path(nonWorkspaceFile.getAbsolutePath())))
                .isEqualTo(new Path(nonWorkspaceFile.getAbsolutePath()));
    }

    @Test
    public void workspaceRelativePathIsCreated_whenPathIsFromProjectNotFromWorkspace() throws Exception {
        final File projectLocation = tempFolder.newFolder("PROJECT_OUT_OF_WORKSPACE");
        final IFolder outsideProjectDir = outsideProjectProvider.createDir("outsideFolder");
        final IFile outsideProjectFile = outsideProjectProvider.createFile("outsideFolder/outsideFile.txt");

        outsideProjectProvider.move(projectLocation);

        assertThat(Paths.toWorkspaceRelativeIfPossible(new Path(outsideProjectDir.getLocation().toOSString())))
                .isEqualTo(new Path(PROJECT_NAME + "_OUTSIDE/outsideFolder"));
        assertThat(Paths.toWorkspaceRelativeIfPossible(new Path(outsideProjectFile.getLocation().toOSString())))
                .isEqualTo(new Path(PROJECT_NAME + "_OUTSIDE/outsideFolder/outsideFile.txt"));
    }

    @Test
    public void absolutePathIsCreated_whenWorkspacePathIsAbsolute() throws Exception {
        assertThat(Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(workspaceDir.getLocation().toOSString())))
                .isEqualTo(new Path(workspaceDir.getLocation().toOSString()));
        assertThat(Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(workspaceFile.getLocation().toOSString())))
                .isEqualTo(new Path(workspaceFile.getLocation().toOSString()));
    }

    @Test
    public void absolutePathIsCreated_whenNonWorkspacePathIsAbsolute() throws Exception {
        assertThat(Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(nonWorkspaceDir.getAbsolutePath())))
                .isEqualTo(new Path(nonWorkspaceDir.getAbsolutePath()));
        assertThat(Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(nonWorkspaceFile.getAbsolutePath())))
                .isEqualTo(new Path(nonWorkspaceFile.getAbsolutePath()));
    }

    @Test
    public void absolutePathIsCreated_whenWorkspacePathIsRelativeToWorkspace() throws Exception {
        assertThat(Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(PROJECT_NAME + "/folder")))
                .isEqualTo(new Path(workspaceDir.getLocation().toOSString()));
        assertThat(Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(PROJECT_NAME + "/folder/file.txt")))
                .isEqualTo(new Path(workspaceFile.getLocation().toOSString()));
    }

    @Test
    public void absolutePathIsCreated_whenWorkspacePathIsRelativeToProject() throws Exception {
        assertThat(Paths.toAbsoluteFromRelativeIfPossible(project, new Path("folder")))
                .isEqualTo(new Path(workspaceDir.getLocation().toOSString()));
        assertThat(Paths.toAbsoluteFromRelativeIfPossible(project, new Path("folder/file.txt")))
                .isEqualTo(new Path(workspaceFile.getLocation().toOSString()));
    }

    @Test
    public void trueIsReturned_whenPathIsCorrect() throws Exception {
        assertThat(Paths.isCorrect(new Path("correct.txt"))).isTrue();
        assertThat(Paths.isCorrect(new Path("a/b/c/correct.txt"))).isTrue();
        assertThat(Paths.isCorrect(new Path("a/b/c/"))).isTrue();
    }

    @Test
    public void falseIsReturned_whenPathIsIncorrect() throws Exception {
        assertThat(Paths.isCorrect(new Path("a/b/${incorrect}/c"))).isFalse();
    }
}
