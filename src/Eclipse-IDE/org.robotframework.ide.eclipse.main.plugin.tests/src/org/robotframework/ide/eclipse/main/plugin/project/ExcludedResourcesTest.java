/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createDir;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class ExcludedResourcesTest {

    @Project
    static IProject project;

    @Test
    public void testResourceVisibility() throws Exception {
        final IFolder visibleDir = createDir(project, "visible_dir");
        final IFolder hiddenDir1 = createDir(project, "visible_dir/.hidden_nested");
        final IFolder hiddenDir2 = createDir(project, ".hidden_dir");
        final IFolder hiddenDir3 = createDir(project, ".hidden_dir/nested");

        final IFile visible1 = createFile(project, "suite.robot");
        final IFile visible2 = createFile(project, "visible_dir/suite.robot");
        final IFile hidden1 = createFile(project, ".hidden.robot");
        final IFile hidden2 = createFile(project, ".hidden_dir/suite.robot");
        final IFile hidden3 = createFile(project, ".hidden_dir/nested/suite.robot");
        final IFile hidden4 = createFile(project, "visible_dir/.hidden_nested/suite.robot");

        assertThat(ExcludedResources.isHiddenInEclipse(visibleDir)).isFalse();
        assertThat(ExcludedResources.isHiddenInEclipse(visible1)).isFalse();
        assertThat(ExcludedResources.isHiddenInEclipse(visible2)).isFalse();
        assertThat(ExcludedResources.isHiddenInEclipse(hiddenDir1)).isTrue();
        assertThat(ExcludedResources.isHiddenInEclipse(hiddenDir2)).isTrue();
        assertThat(ExcludedResources.isHiddenInEclipse(hiddenDir3)).isTrue();
        assertThat(ExcludedResources.isHiddenInEclipse(hidden1)).isTrue();
        assertThat(ExcludedResources.isHiddenInEclipse(hidden2)).isTrue();
        assertThat(ExcludedResources.isHiddenInEclipse(hidden3)).isTrue();
        assertThat(ExcludedResources.isHiddenInEclipse(hidden4)).isTrue();
    }

    @Test
    public void testPathExcluding() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.addExcludedPath("excluded.robot");
        projectConfig.addExcludedPath("exincluded_dir");

        final IFolder includedDir = createDir(project, "included_dir");
        final IFolder excludedDir1 = createDir(project, "exincluded_dir");
        final IFolder excludedDir2 = createDir(project, "exincluded_dir/nested");

        final IFile included1 = createFile(project, "included.robot");
        final IFile included2 = createFile(project, "included_dir/suite.robot");
        final IFile excluded1 = createFile(project, "excluded.robot");
        final IFile excluded2 = createFile(project, "exincluded_dir/suite.robot");
        final IFile excluded3 = createFile(project, "exincluded_dir/nested/suite.robot");

        assertThat(ExcludedResources.isInsideExcludedPath(includedDir, projectConfig)).isFalse();
        assertThat(ExcludedResources.isInsideExcludedPath(included1, projectConfig)).isFalse();
        assertThat(ExcludedResources.isInsideExcludedPath(included2, projectConfig)).isFalse();
        assertThat(ExcludedResources.isInsideExcludedPath(excludedDir1, projectConfig)).isTrue();
        assertThat(ExcludedResources.isInsideExcludedPath(excludedDir2, projectConfig)).isTrue();
        assertThat(ExcludedResources.isInsideExcludedPath(excluded1, projectConfig)).isTrue();
        assertThat(ExcludedResources.isInsideExcludedPath(excluded2, projectConfig)).isTrue();
        assertThat(ExcludedResources.isInsideExcludedPath(excluded3, projectConfig)).isTrue();
    }

    @Test
    public void testRequiredSize_whenFileSizeCheckingIsDisabled() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setIsValidatedFileSizeCheckingEnabled(false);

        final IFile suite = createFile(project, "suite.robot",
                String.join("", Collections.nCopies(1025 * 1024, "a")));

        assertThat(ExcludedResources.hasRequiredSize(suite, projectConfig)).isTrue();
    }

    @Test
    public void testRequiredSize_whenFileSizeCheckingEnabledAndCorrectSizeIsSet() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setIsValidatedFileSizeCheckingEnabled(true);
        projectConfig.setValidatedFileMaxSize("2");

        final IFile suite1KB = createFile(project, "suite1KB.robot",
                String.join("", Collections.nCopies(1 * 1024, "a")));

        final IFile suite2KB = createFile(project, "suite2KB.robot",
                String.join("", Collections.nCopies(2 * 1024, "a")));

        final IFile suite3KB = createFile(project, "suite3KB.robot",
                String.join("", Collections.nCopies(3 * 1024, "a")));

        assertThat(ExcludedResources.hasRequiredSize(suite1KB, projectConfig)).isTrue();
        assertThat(ExcludedResources.hasRequiredSize(suite2KB, projectConfig)).isTrue();
        assertThat(ExcludedResources.hasRequiredSize(suite3KB, projectConfig)).isFalse();
    }

    @Test
    public void testRequiredSize_whenFileSizeCheckingEnabledAndIncorrectSizeIsSet() throws Exception {
        final RobotProjectConfig projectConfig = new RobotProjectConfig();
        projectConfig.setIsValidatedFileSizeCheckingEnabled(true);
        projectConfig.setValidatedFileMaxSize("invalid");

        final IFile suite1023KB = createFile(project, "suite1023KB.robot",
                String.join("", Collections.nCopies(1023 * 1024, "a")));

        final IFile suite1024KB = createFile(project, "suite1024KB.robot",
                String.join("", Collections.nCopies(1024 * 1024, "a")));

        final IFile suite1025KB = createFile(project, "suite1025KB.robot",
                String.join("", Collections.nCopies(1025 * 1024, "a")));

        assertThat(ExcludedResources.hasRequiredSize(suite1023KB, projectConfig)).isTrue();
        assertThat(ExcludedResources.hasRequiredSize(suite1024KB, projectConfig)).isTrue();
        assertThat(ExcludedResources.hasRequiredSize(suite1025KB, projectConfig)).isFalse();
    }

}
