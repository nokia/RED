/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.executor.RedSystemProperties;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.red.junit.ProjectProvider;

public class CreateResourceFileFixerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(CreateResourceFileFixerTest.class);

    private static RobotSuiteFile topLevelFile;

    private static String workspaceDir;

    private static IMarker marker;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("dir1");
        projectProvider.createDir("dir1/dir2");
        projectProvider.createDir("dir1/dir2/dir3");

        topLevelFile = new RobotModel()
                .createSuiteFile(projectProvider.createFile("dir1/file1.robot", "*** Keywords ***"));

        marker = topLevelFile.getFile().createMarker(RedPlugin.PLUGIN_ID);
        workspaceDir = marker.getResource().getWorkspace().getRoot().getLocation().toPortableString();
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "res.robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertNotNull(path);
        assertEquals(path.matchingFirstSegments(new Path("CreateResourceFileFixerTest/dir1/res.robot")),
                path.segmentCount());
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalNestedPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "dir2/dir3/res.robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertNotNull(path);
        assertEquals(path.matchingFirstSegments(new Path("CreateResourceFileFixerTest/dir1/dir2/dir3/res.robot")),
                path.segmentCount());
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalNestedNonExistingPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "../dir1/non/existing/path/res.robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertNotNull(path);
        assertEquals(
                path.matchingFirstSegments(new Path("CreateResourceFileFixerTest/dir1/non/existing/path/res.robot")),
                path.segmentCount());
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalWithSpecialCharsPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "do!@#$% ^&()/res !@#$%^&().robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertNotNull(path);
        assertEquals(path.matchingFirstSegments(new Path("CreateResourceFileFixerTest/dir1/do!@#$% ^&()/res !@#$%^&().robot")), path.segmentCount());
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalInProjectPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "../res.robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertNotNull(path);
        assertEquals(path.matchingFirstSegments(new Path("CreateResourceFileFixerTest/res.robot")),
                path.segmentCount());
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalBackToProjectPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH,
                "../../" + projectProvider.getProject().getName() + "/res.robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertNotNull(path);
        assertEquals(path.matchingFirstSegments(new Path("CreateResourceFileFixerTest/res.robot")),
                path.segmentCount());
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeButNonExistingProjectPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "../../fictionalProject/res.robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertNull(path);
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeButOutOfWorkspacePath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "../../res.robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertNull(path);
    }

    @Test
    public void testGetValidPathToCreate_whenIllegalCharactersPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "illeg*l/res.robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        if (RedSystemProperties.isWindowsPlatform()) {
            assertNull(path);
        } else {
            assertNotNull(path);
            assertEquals(path.matchingFirstSegments(new Path("CreateResourceFileFixerTest/dir1/illeg*l/res.robot")),
                    path.segmentCount());
        }
    }

    @Test
    public void testGetValidPathToCreate_whenAbsoluteAndValidPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH,
                    workspaceDir + "/" + projectProvider.getProject().getName() + "/res.robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertNotNull(path);
        assertEquals(path.matchingFirstSegments(new Path("CreateResourceFileFixerTest/res.robot")),
                path.segmentCount());
    }

    @Test
    public void testGetValidPathToCreate_whenAbsoluteAndInWorkspaceNonCanonicalPath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH,
                    workspaceDir + "/" + projectProvider.getProject().getName() + "/dir1/../dir1/res.robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertNotNull(path);
        assertEquals(path.matchingFirstSegments(new Path("CreateResourceFileFixerTest/dir1/res.robot")),
                path.segmentCount());
    }

    @Test
    public void testGetValidPathToCreate_whenAbsoluteButOutOfWorkspacePath() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, workspaceDir + "/res.robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertNull(path);
    }

    @Test
    public void testGetValidPathToCreate_whenAbsolutePathFromAnotherSystem() throws Exception {
        final String anotherSystemWorkspace = RedSystemProperties.isWindowsPlatform() ? workspaceDir.substring(2)
                : "D:" + workspaceDir;
        marker.setAttribute(AdditionalMarkerAttributes.PATH,
                anotherSystemWorkspace + "/" + projectProvider.getProject().getName() + "/res.robot");
        final IPath path = CreateResourceFileFixer.getValidPathToCreate(marker);
        assertNull(path);
    }

    @Test
    public void testCreateFixer_whenCorrectMarkerAndName() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "res.robot");
        final CreateResourceFileFixer fixer = CreateResourceFileFixer.createFixer("res.robot", marker);
        assertNotNull(fixer);
        assertEquals(fixer.getLabel(), "Create missing res.robot file");
    }

    @Test
    public void testCreateFixer_whenIncorrectMarkerOrName() throws Exception {
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "/../res.robot");
        final CreateResourceFileFixer fixer = CreateResourceFileFixer.createFixer("res.robot", marker);
        assertNotNull(fixer);
        assertEquals(fixer.getLabel(), "Missing resource file cannot be auto-created");
    }
}
