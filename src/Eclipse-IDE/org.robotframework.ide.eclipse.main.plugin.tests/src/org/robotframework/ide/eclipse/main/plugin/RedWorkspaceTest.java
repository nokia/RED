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
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace.Paths;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.RedTempDirectory;
import org.robotframework.red.junit.jupiter.StatefulProject;

@ExtendWith({ ProjectExtension.class, RedTempDirectory.class })
public class RedWorkspaceTest {

    @Project(dirs = { "folder" }, files = { "folder/file.txt" })
    static StatefulProject project;

    @Project(nameSuffix = "_OUTSIDE")
    StatefulProject outsideProject;

    @TempDir
    static File tempFolder;

    private static IFolder workspaceDir;

    private static IFile workspaceFile;

    private static File nonWorkspaceDir;

    private static File nonWorkspaceFile;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        workspaceDir = project.getDir("folder");
        workspaceFile = project.getFile("folder/file.txt");
        nonWorkspaceDir = RedTempDirectory.createNewDir(tempFolder, "nonWorkspaceFolder");
        nonWorkspaceFile = RedTempDirectory.createNewFile(tempFolder, "nonWorkspaceFolder/nonWorkspaceFile.txt");
    }

    @Test
    public void workspaceRelativePathIsCreated_whenPathIsFromWorkspace() throws Exception {
        assertThat(Paths.toWorkspaceRelativeIfPossible(new Path(workspaceDir.getLocation().toOSString())))
                .isEqualTo(new Path(project.getName() + "/folder"));
        assertThat(Paths.toWorkspaceRelativeIfPossible(new Path(workspaceFile.getLocation().toOSString())))
                .isEqualTo(new Path(project.getName() + "/folder/file.txt"));
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
        final File projectLocation = RedTempDirectory.createNewDir(tempFolder, "PROJECT_OUT_OF_WORKSPACE");
        final IFolder outsideProjectDir = outsideProject.createDir("outsideFolder");
        final IFile outsideProjectFile = outsideProject.createFile("outsideFolder/outsideFile.txt");

        outsideProject.move(projectLocation);

        assertThat(Paths.toWorkspaceRelativeIfPossible(new Path(outsideProjectDir.getLocation().toOSString())))
                .isEqualTo(new Path(project.getName() + "_OUTSIDE/outsideFolder"));
        assertThat(Paths.toWorkspaceRelativeIfPossible(new Path(outsideProjectFile.getLocation().toOSString())))
                .isEqualTo(new Path(project.getName() + "_OUTSIDE/outsideFolder/outsideFile.txt"));
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
        assertThat(Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(project.getName() + "/folder")))
                .isEqualTo(new Path(workspaceDir.getLocation().toOSString()));
        assertThat(Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(project.getName() + "/folder/file.txt")))
                .isEqualTo(new Path(workspaceFile.getLocation().toOSString()));
    }

    @Test
    public void absolutePathIsCreated_whenWorkspacePathIsRelativeToProject() throws Exception {
        assertThat(Paths.toAbsoluteFromRelativeIfPossible(project.getProject(), new Path("folder")))
                .isEqualTo(new Path(workspaceDir.getLocation().toOSString()));
        assertThat(Paths.toAbsoluteFromRelativeIfPossible(project.getProject(), new Path("folder/file.txt")))
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
