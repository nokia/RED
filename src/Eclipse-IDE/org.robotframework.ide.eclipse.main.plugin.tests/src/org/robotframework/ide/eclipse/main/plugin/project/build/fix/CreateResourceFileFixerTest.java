/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.red.junit.ProjectProvider;

public class CreateResourceFileFixerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(CreateResourceFileFixerTest.class);

    private static final IMarker mockedMarker = mock(IMarker.class);

    private static RobotSuiteFile topLevelFile;

    private static boolean isWindows;

    private static String workspaceDir;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        isWindows = System.getProperty("os.name").startsWith("Windows");
        workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toPortableString();

        projectProvider.createDir("dir1");
        projectProvider.createDir("dir1/dir2");
        projectProvider.createDir("dir1/dir2/dir3");

        final RobotModelManager modelManager = RedPlugin.getModelManager();
        topLevelFile = modelManager.createSuiteFile(projectProvider.createFile("dir1/file1.robot", "*** Keywords ***"));

        IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), topLevelFile.getFile(),
                true);
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalPath() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null)).thenReturn("res.robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        assertNotNull(path);
        assertEquals(path.toPortableString(), "file:CreateResourceFileFixerTest/dir1/res.robot");
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalNestedPath() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null)).thenReturn("dir2/dir3/res.robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        assertNotNull(path);
        assertEquals(path.toPortableString(), "file:CreateResourceFileFixerTest/dir1/dir2/dir3/res.robot");
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalNestedNonExistingPath() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null))
                .thenReturn("../dir1/non/existing/path/res.robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        assertNotNull(path);
        assertEquals(path.toPortableString(), "file:CreateResourceFileFixerTest/dir1/non/existing/path/res.robot");
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalWithSpecialCharsPath() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null))
                .thenReturn("do!@#$% ^&()/res !@#$%^&().robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        assertNotNull(path);
        assertEquals(path.toPortableString(), "file:CreateResourceFileFixerTest/dir1/do!@#$% ^&()/res !@#$%^&().robot");
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalInProjectPath() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null)).thenReturn("../res.robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        assertNotNull(path);
        assertEquals(path.toPortableString(), "file:CreateResourceFileFixerTest/res.robot");
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeAndLegalBackToProjectPath() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null))
                .thenReturn("../../" + projectProvider.getProject().getName() + "/res.robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        assertNotNull(path);
        assertEquals(path.toPortableString(), "file:CreateResourceFileFixerTest/res.robot");
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeButNonExistingProjectPath() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null))
                .thenReturn("../../fictionalProject/res.robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        assertNull(path);
    }

    @Test
    public void testGetValidPathToCreate_whenRelativeButOutOfWorkspacePath() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null)).thenReturn("../../res.robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        assertNull(path);
    }

    @Test
    public void testGetValidPathToCreate_whenIllegalCharactersPath() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null)).thenReturn("illeg*l/res.robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        if (isWindows) {
            assertNull(path);
        } else {
            assertNotNull(path);
            assertEquals(path.toPortableString(), "file:CreateResourceFileFixerTest/illeg*l/res.robot");
        }
    }

    @Test
    public void testGetValidPathToCreate_whenAbsoluteAndValidPath() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null))
                .thenReturn(workspaceDir + "/" + projectProvider.getProject().getName() + "/res.robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        assertNotNull(path);
        assertEquals(path.toPortableString(), "file:CreateResourceFileFixerTest/res.robot");
    }

    @Test
    public void testGetValidPathToCreate_whenAbsoluteAndInWorkspaceNonCononicalPath() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null))
                .thenReturn(workspaceDir + "/" + projectProvider.getProject().getName() + "/dir1/../dir1/res.robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        assertNotNull(path);
        assertEquals(path.toPortableString(), "file:CreateResourceFileFixerTest/dir1/res.robot");
    }

    @Test
    public void testGetValidPathToCreate_whenAbsoluteButOutOfWorkspacePath() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null)).thenReturn(workspaceDir + "/res.robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        assertNull(path);
    }

    @Test
    public void testGetValidPathToCreate_whenAbsolutePathFromAnotherSystem() {
        final String anotherSystemWorkspace = isWindows ? workspaceDir.substring(2) : "D:" + workspaceDir;
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null))
                .thenReturn(anotherSystemWorkspace + "/" + projectProvider.getProject().getName() + "/res.robot");
        IPath path = CreateResourceFileFixer.getValidPathToCreate(mockedMarker);
        assertNull(path);
    }

    @Test
    public void testCreateFixer_whenCorrectMarkerAndName() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null)).thenReturn("res.robot");
        CreateResourceFileFixer fixer = CreateResourceFileFixer.createFixer("res.robot", mockedMarker);
        assertNotNull(fixer);
        assertEquals(fixer.getLabel(), "Create missing res.robot file");
    }

    @Test
    public void testCreateFixer_whenIncorrectMarkerOrName() {
        when(mockedMarker.getAttribute(AdditionalMarkerAttributes.PATH, null)).thenReturn("/../res.robot");
        CreateResourceFileFixer fixer = CreateResourceFileFixer.createFixer("res.robot", mockedMarker);
        assertNotNull(fixer);
        assertEquals(fixer.getLabel(), "Missing resource file cannot be auto-created");
    }

}
