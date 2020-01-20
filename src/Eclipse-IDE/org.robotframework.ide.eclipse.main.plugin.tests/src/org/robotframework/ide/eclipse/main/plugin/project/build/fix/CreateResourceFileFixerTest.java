/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class CreateResourceFileFixerTest {

    @Project(dirs = { "dir1", "dir1/dir2", "dir1/dir2/dir3" })
    static IProject project;

    private static RobotSuiteFile topLevelFile;

    private static String workspaceDir;

    private static IMarker marker;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        topLevelFile = new RobotModel().createSuiteFile(createFile(project, "dir1/file1.robot", "*** Keywords ***"));

        marker = topLevelFile.getFile().createMarker(RedPlugin.PLUGIN_ID);
        workspaceDir = marker.getResource().getWorkspace().getRoot().getLocation().toPortableString();
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).hasValueSatisfying(equalSegmentCountRequirement("CreateResourceFileFixerTest/dir1/res.robot"));
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalNestedPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "dir2/dir3/res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).hasValueSatisfying(
                equalSegmentCountRequirement("CreateResourceFileFixerTest/dir1/dir2/dir3/res.robot"));
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalNestedNonExistingPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "../dir1/non/existing/path/res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).hasValueSatisfying(
                equalSegmentCountRequirement("CreateResourceFileFixerTest/dir1/non/existing/path/res.robot"));
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalWithSpecialCharsPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "do!@#$% ^&()/res !@#$%^&().robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).hasValueSatisfying(
                equalSegmentCountRequirement("CreateResourceFileFixerTest/dir1/do!@#$% ^&()/res !@#$%^&().robot"));
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalInProjectPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "../res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).hasValueSatisfying(equalSegmentCountRequirement("CreateResourceFileFixerTest/res.robot"));
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalBackToProjectPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH,
                "../../" + project.getName() + "/res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).hasValueSatisfying(equalSegmentCountRequirement("CreateResourceFileFixerTest/res.robot"));
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeButNonExistingProjectPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "../../fictionalProject/res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).isNotPresent();
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeButOutOfWorkspacePath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "../../res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).isNotPresent();
    }

    @EnabledOnOs(OS.WINDOWS)
    @Test
    public void testGetValidPathToCreate_whenIllegalCharactersPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "illeg*l/res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).isNotPresent();
    }

    @Test
    public void testGetValidPathToCreate_whenAbsoluteAndValidPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH,
                workspaceDir + "/" + project.getName() + "/res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).hasValueSatisfying(equalSegmentCountRequirement("CreateResourceFileFixerTest/res.robot"));
    }

    @Test
    public void testGetValidPathToCreate_whenAbsoluteAndInWorkspaceNonCanonicalPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH,
                workspaceDir + "/" + project.getName() + "/dir1/../dir1/res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).hasValueSatisfying(equalSegmentCountRequirement("CreateResourceFileFixerTest/dir1/res.robot"));
    }

    @Test
    public void testGetValidPathToCreate_whenAbsoluteButOutOfWorkspacePath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, workspaceDir + "/res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).isNotPresent();
    }

    @EnabledOnOs(OS.WINDOWS)
    @Test
    public void testGetValidPathToCreate_whenAbsolutePathFromAnotherSystem_forWindows() throws Exception {
        final String anotherSystemWorkspace = workspaceDir.substring(2);
        marker.setAttribute(AdditionalMarkerAttributes.PATH,
                anotherSystemWorkspace + "/" + project.getName() + "/res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).isNotPresent();
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void testGetValidPathToCreate_whenAbsolutePathFromAnotherSystem_forLinux() throws Exception {
        final String anotherSystemWorkspace = "D:" + workspaceDir;
        marker.setAttribute(AdditionalMarkerAttributes.PATH,
                anotherSystemWorkspace + "/" + project.getName() + "/res.robot");
        final Optional<IPath> path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertThat(path).isNotPresent();
    }

    @Test
    public void testCreateFixer_whenCorrectMarkerAndName() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "res.robot");
        final CreateResourceFileFixer fixer = CreateResourceFileFixer.createFixer("res.robot", marker);
        assertThat(fixer.getLabel()).isEqualTo("Create missing 'res.robot' file");
    }

    @Test
    public void testCreateFixer_whenIncorrectMarkerOrName() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "/../res.robot");
        final CreateResourceFileFixer fixer = CreateResourceFileFixer.createFixer("res.robot", marker);
        assertThat(fixer.getLabel()).isEqualTo("Missing resource file cannot be auto-created");
    }

    private Consumer<IPath> equalSegmentCountRequirement(final String fullPath) {
        return path -> assertThat(path.segmentCount()).isEqualTo(path.matchingFirstSegments(new Path(fullPath)));
    }
}
